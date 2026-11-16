-- =====================================================================
-- BStudent - migrazione v0.10 (PostgreSQL)
-- Applica alla v0.9: gamification (blueprint/BStudent_Blueprint_Gamification_v0.1.md,
-- decisioni del 16/11/2026).
-- - Palestra: difficolta' delle voci; argomenti: classe di riferimento;
-- - livelli delle skill: curva della beta e livelli di partenza;
-- - accrediti skill: materia; vista dei totali per studente, materia e skill;
-- - personaggi: "iniziale" e motto; regole degli stadi; stadi raggiunti;
-- - XP dello studente: registro per missione superata;
-- - vite ottenute (sfida giornaliera, tris della Palestra);
-- - impostazioni di gioco: sfida, tris, pausa;
-- - corsi: corso d'ingresso tutto gratuito, uno per materia.
-- SOLO AGGIUNTE: il DB e' condiviso con il branch dei corsi, che deve
-- continuare a partire con ddl-auto=validate. Nessuna colonna tolta o
-- rinominata; le colonne nuove sono facoltative o hanno un valore predefinito.
-- Transazionale e rieseguibile.
-- =====================================================================
BEGIN;

-- ---------------------------------------------------------------------
-- 1. Palestra: difficolta' della voce (§3)
-- ---------------------------------------------------------------------
-- Stessa scala dei livelli delle skill. Le voci esistenti partono da 1.
-- Nessun massimo nel CHECK: il tetto si alza aggiungendo livelli (§2.4).
ALTER TABLE practice_exercises ADD COLUMN IF NOT EXISTS difficulty integer NOT NULL DEFAULT 1;
ALTER TABLE practice_exercises DROP CONSTRAINT IF EXISTS chk_practice_exercises_difficulty;
ALTER TABLE practice_exercises ADD  CONSTRAINT chk_practice_exercises_difficulty CHECK (difficulty >= 1);

-- ---------------------------------------------------------------------
-- 2. Argomenti: classe di riferimento (§3-bis)
-- ---------------------------------------------------------------------
-- Anno di scuola superiore da cui di solito si studia l'argomento (1-5).
-- Vuoto = per tutti. Non blocca: produce solo un avviso.
ALTER TABLE topics ADD COLUMN IF NOT EXISTS reference_year smallint;
ALTER TABLE topics DROP CONSTRAINT IF EXISTS chk_topics_reference_year;
ALTER TABLE topics ADD  CONSTRAINT chk_topics_reference_year
  CHECK (reference_year IS NULL OR reference_year BETWEEN 1 AND 5);

-- ---------------------------------------------------------------------
-- 3. Livelli delle skill (§2)
-- ---------------------------------------------------------------------
-- Curva della beta, uguale per tutte le skill: 0, 20, 50, 95, 165, 265.
-- Resta una riga per skill e livello, cosi' un'eccezione per una skill e'
-- possibile. Si inserisce solo nelle skill che non hanno ancora livelli:
-- le soglie gia' scritte a mano non vengono toccate.
INSERT INTO skill_levels (skill_id, level_number, min_points)
SELECT s.skill_id, c.level_number, c.min_points
  FROM skills s
 CROSS JOIN (VALUES (1, 0.00), (2, 20.00), (3, 50.00), (4, 95.00), (5, 165.00), (6, 265.00))
       AS c (level_number, min_points)
 WHERE NOT EXISTS (SELECT 1 FROM skill_levels l WHERE l.skill_id = s.skill_id);

-- 3.1 Livello di partenza dal test di orientamento (§2.6). Vale come punti
-- di partenza pari alla soglia del livello, usati solo per il livello.
CREATE TABLE IF NOT EXISTS student_starting_levels (
  student_id   bigint      NOT NULL REFERENCES students (student_id),
  subject_id   bigint      NOT NULL REFERENCES subjects (subject_id),
  skill_id     bigint      NOT NULL REFERENCES skills (skill_id),
  level_number integer     NOT NULL,
  set_at       timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (student_id, subject_id, skill_id),
  CONSTRAINT chk_student_starting_levels_level CHECK (level_number >= 1)
);
CREATE INDEX IF NOT EXISTS idx_student_starting_levels_subject ON student_starting_levels (subject_id);
CREATE INDEX IF NOT EXISTS idx_student_starting_levels_skill   ON student_starting_levels (skill_id);

-- ---------------------------------------------------------------------
-- 4. Accrediti skill: materia e vista dei totali (§10.4-ter del blueprint principale)
-- ---------------------------------------------------------------------
-- Punti e livelli si contano per materia. La colonna e' facoltativa per non
-- rompere il branch dei corsi; il Blocco 5 la compila a ogni accredito.
-- Prima del Blocco 5 non esistono accrediti: nessun dato da ricostruire.
ALTER TABLE skill_credit_entries ADD COLUMN IF NOT EXISTS subject_id bigint REFERENCES subjects (subject_id);
CREATE INDEX IF NOT EXISTS idx_skill_credit_entries_subject
  ON skill_credit_entries (student_id, subject_id, skill_id);

DO $$
DECLARE
  n bigint;
BEGIN
  SELECT count(*) INTO n FROM skill_credit_entries WHERE subject_id IS NULL;
  IF n > 0 THEN
    RAISE NOTICE 'Attenzione: % accrediti skill senza materia. Vanno assegnati a mano prima di usare i livelli.', n;
  END IF;
END $$;

-- Totali per studente, materia, skill e origine (practice / mission).
-- Gli accrediti senza materia restano visibili con subject_id vuoto.
CREATE OR REPLACE VIEW student_skill_totals AS
SELECT student_id,
       subject_id,
       skill_id,
       source_kind,
       sum(delta) AS total_points
  FROM skill_credit_entries
 GROUP BY student_id, subject_id, skill_id, source_kind;

-- ---------------------------------------------------------------------
-- 5. Personaggi (§5)
-- ---------------------------------------------------------------------
-- "Iniziale" = si puo' scegliere alla registrazione e cambiare liberamente.
-- Gli altri personaggi pubblicati compaiono come "Prossimamente".
-- I personaggi esistenti partono NON iniziali: li indica l'Admin.
ALTER TABLE skins ADD COLUMN IF NOT EXISTS is_starter boolean NOT NULL DEFAULT false;
ALTER TABLE skins ADD COLUMN IF NOT EXISTS motto      text;

-- ---------------------------------------------------------------------
-- 6. Stadi dell'evoluzione (§4)
-- ---------------------------------------------------------------------
-- Regole uguali per tutti i personaggi: per questo stanno in una tabella a
-- parte e non su skin_evolutions (che tiene le immagini di ogni personaggio).
--   start        = stadio di partenza
--   first_course = primo corso completato (regola a se', base vite da 5 a 7)
--   thresholds   = XP minimi E livello medio minimo nella materia migliore
CREATE TABLE IF NOT EXISTS evolution_stage_rules (
  stage_number      integer       PRIMARY KEY,
  rule_kind         text          NOT NULL,
  min_xp            numeric(12,2),
  min_average_level numeric(4,2),
  CONSTRAINT chk_evolution_stage_rules_stage CHECK (stage_number >= 0),
  CONSTRAINT chk_evolution_stage_rules_kind  CHECK (rule_kind IN ('start', 'first_course', 'thresholds')),
  CONSTRAINT chk_evolution_stage_rules_thresholds CHECK (
    (rule_kind = 'thresholds' AND min_xp IS NOT NULL AND min_xp >= 0
                              AND min_average_level IS NOT NULL AND min_average_level >= 1)
    OR (rule_kind <> 'thresholds' AND min_xp IS NULL AND min_average_level IS NULL))
);

-- Stadi della beta: Base, Evoluzione I, Evoluzione II (800 XP e media 3).
-- Solo se la tabella e' vuota: i valori cambiati dall'Admin restano.
INSERT INTO evolution_stage_rules (stage_number, rule_kind, min_xp, min_average_level)
SELECT v.stage_number, v.rule_kind, v.min_xp, v.min_average_level
  FROM (VALUES (0, 'start',        NULL::numeric, NULL::numeric),
               (1, 'first_course', NULL,          NULL),
               (2, 'thresholds',   800.00,        3.00))
       AS v (stage_number, rule_kind, min_xp, min_average_level)
 WHERE NOT EXISTS (SELECT 1 FROM evolution_stage_rules);

-- 6.1 Stadi raggiunti: dello studente, non del personaggio (§4.5, §4.6).
-- Una riga non si cancella mai: lo stadio raggiunto non si perde.
CREATE TABLE IF NOT EXISTS student_evolution_stages (
  student_id   bigint      NOT NULL REFERENCES students (student_id),
  stage_number integer     NOT NULL,
  reached_at   timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (student_id, stage_number),
  CONSTRAINT chk_student_evolution_stages_stage CHECK (stage_number >= 1)
);

-- ---------------------------------------------------------------------
-- 7. XP dello studente (§1)
-- ---------------------------------------------------------------------
-- Una riga per missione superata la prima volta. Gli XP si fissano in quel
-- momento: cambiare poi i punti degli esercizi non cambia gli XP guadagnati.
-- UNIQUE (studente, missione): ripetizioni e doppi invii non riaccreditano.
CREATE TABLE IF NOT EXISTS student_xp_entries (
  xp_entry_id        bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  student_id         bigint        NOT NULL REFERENCES students (student_id),
  mission_id         bigint        NOT NULL REFERENCES missions (mission_id),
  mission_attempt_id bigint        NOT NULL,
  xp                 numeric(12,2) NOT NULL,
  awarded_at         timestamptz   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT student_xp_entries_student_id_mission_id_idx UNIQUE (student_id, mission_id),
  CONSTRAINT chk_student_xp_entries_xp CHECK (xp > 0)
);
CREATE INDEX IF NOT EXISTS idx_student_xp_entries_mission ON student_xp_entries (mission_id);

-- ---------------------------------------------------------------------
-- 8. Vite ottenute (§7.1, §8.1)
-- ---------------------------------------------------------------------
--   daily_challenge = sfida giornaliera completata (una al giorno)
--   practice_streak = tre voci della Palestra superate di fila
-- event_id unico: lo stesso evento non assegna due vite.
-- award_day = giorno secondo l'ora italiana, calcolato dal service.
CREATE TABLE IF NOT EXISTS life_awards (
  life_award_id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  student_id    bigint      NOT NULL REFERENCES students (student_id),
  reason        text        NOT NULL,
  award_day     date        NOT NULL,
  event_id      text        NOT NULL,
  awarded_at    timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT life_awards_event_id_idx UNIQUE (event_id),
  CONSTRAINT chk_life_awards_reason CHECK (reason IN ('daily_challenge', 'practice_streak'))
);
CREATE INDEX IF NOT EXISTS idx_life_awards_student ON life_awards (student_id, award_day);
-- Una sola vita della sfida per studente e giorno
CREATE UNIQUE INDEX IF NOT EXISTS life_awards_daily_challenge_idx
  ON life_awards (student_id, award_day) WHERE reason = 'daily_challenge';

-- ---------------------------------------------------------------------
-- 9. Impostazioni di gioco (§7, §8.1, §8.1-bis)
-- ---------------------------------------------------------------------
ALTER TABLE gamification_settings ADD COLUMN IF NOT EXISTS daily_challenge_target   integer NOT NULL DEFAULT 3;
ALTER TABLE gamification_settings ADD COLUMN IF NOT EXISTS practice_streak_for_life integer NOT NULL DEFAULT 3;
ALTER TABLE gamification_settings ADD COLUMN IF NOT EXISTS practice_fails_for_pause integer NOT NULL DEFAULT 3;
ALTER TABLE gamification_settings DROP CONSTRAINT IF EXISTS chk_gamification_settings_goals;
ALTER TABLE gamification_settings ADD  CONSTRAINT chk_gamification_settings_goals
  CHECK (daily_challenge_target > 0 AND practice_streak_for_life > 0 AND practice_fails_for_pause > 0);

-- ---------------------------------------------------------------------
-- 10. Corso d'ingresso (§8-bis)
-- ---------------------------------------------------------------------
-- Uno per materia, gratuito dall'inizio alla fine: da' la prima evoluzione
-- a chiunque. Che sia free e senza missioni premium lo controlla il service
-- (regola tra piu' tabelle). I corsi esistenti partono NON d'ingresso.
ALTER TABLE courses ADD COLUMN IF NOT EXISTS is_entry_course boolean NOT NULL DEFAULT false;
CREATE UNIQUE INDEX IF NOT EXISTS courses_entry_course_per_subject_idx
  ON courses (subject_id) WHERE is_entry_course;

COMMIT;
