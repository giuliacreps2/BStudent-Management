-- =====================================================================
-- BStudent - migrazione v0.6 (PostgreSQL)
-- Applica alla v0.5: core dell'Academy (corsi, missioni, esercizi).
-- - Stati editoriali draft/published/withdrawn su corsi, missioni,
--   lezioni, esercizi e voci Palestra (withdrawn_at resta la data del ritiro);
-- - versione pubblicata dell'esercizio e versione svolta nel tentativo;
-- - configurazione dell'esercizio di missione: tempo, punti massimi, pesi skill;
-- - skill predefinita della materia (fallback 100% dei pesi);
-- - freemium anche sulla Palestra e valori di access_tier vincolati;
-- - modalita' di valutazione e rubriche della domanda;
-- - impostazioni dell'Academy (soglia, fattore skill, default);
-- - vincoli di ordinamento DEFERRABLE per il riordino atomico.
-- Transazionale e rieseguibile (se un dato esistente viola un nuovo vincolo,
-- la migrazione si interrompe e non cambia nulla).
-- =====================================================================
BEGIN;

-- Helper temporaneo: elimina ogni unicita' esistente su esattamente quelle
-- colonne, sia vincolo UNIQUE (nel DB reale: *_key) sia indice unico libero
-- (CREATE UNIQUE INDEX senza nome), cosi' non dipende dai nomi generati.
CREATE FUNCTION pg_temp.drop_uniques_on(p_table regclass, p_columns text)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  r record;
BEGIN
  FOR r IN
    SELECT conname FROM pg_constraint
    WHERE conrelid = p_table AND contype = 'u'
      -- Anche "UNIQUE (...) DEFERRABLE" di un'esecuzione precedente
      AND pg_get_constraintdef(oid) LIKE format('UNIQUE (%s)%%', p_columns)
  LOOP
    EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', p_table, r.conname);
  END LOOP;

  FOR r IN
    SELECT i.indexrelid::regclass AS index_name
    FROM pg_index i
    WHERE i.indrelid = p_table AND i.indisunique AND NOT i.indisprimary
      AND NOT EXISTS (SELECT 1 FROM pg_constraint c WHERE c.conindid = i.indexrelid)
      AND i.indpred IS NULL
      AND pg_get_indexdef(i.indexrelid) ILIKE format('%% (%s)', p_columns)
  LOOP
    EXECUTE format('DROP INDEX %s', r.index_name);
  END LOOP;
END $$;

-- ---------------------------------------------------------------------
-- 1. Stati editoriali (draft -> published -> withdrawn)
-- ---------------------------------------------------------------------
-- draft: visibile solo all'Admin; published: visibile agli studenti
-- (soggetto ad access_tier); withdrawn: nascosto ma conservato per lo
-- storico dei progressi. Lo studente vede un elemento solo se anche i suoi
-- contenitori sono published: la regola e' nei service, non in un CHECK.
-- I contenuti esistenti partono da draft (o withdrawn se gia' ritirati):
-- nulla diventa visibile finche' l'Admin non lo pubblica.
DO $$
DECLARE
  t text;
BEGIN
  FOREACH t IN ARRAY ARRAY['courses', 'missions', 'lessons', 'question_bank_entries', 'practice_exercises']
  LOOP
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS status text NOT NULL DEFAULT ''draft''', t);
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS published_at timestamptz', t);
    EXECUTE format('UPDATE %I SET status = ''withdrawn'' WHERE withdrawn_at IS NOT NULL AND status <> ''withdrawn''', t);

    EXECUTE format('ALTER TABLE %I DROP CONSTRAINT IF EXISTS %I', t, 'chk_' || t || '_status');
    EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I CHECK (status IN (''draft'', ''published'', ''withdrawn''))',
                   t, 'chk_' || t || '_status');

    -- withdrawn_at e' la data del ritiro: presente se e solo se ritirato
    EXECUTE format('ALTER TABLE %I DROP CONSTRAINT IF EXISTS %I', t, 'chk_' || t || '_withdrawn');
    EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I CHECK ((status = ''withdrawn'') = (withdrawn_at IS NOT NULL))',
                   t, 'chk_' || t || '_withdrawn');

    -- published_at resta anche dopo il ritiro (prima pubblicazione)
    EXECUTE format('ALTER TABLE %I DROP CONSTRAINT IF EXISTS %I', t, 'chk_' || t || '_published');
    EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I CHECK (status <> ''published'' OR published_at IS NOT NULL)',
                   t, 'chk_' || t || '_published');
  END LOOP;
END $$;

-- ---------------------------------------------------------------------
-- 2. Freemium: valori ammessi e Palestra
-- ---------------------------------------------------------------------
-- Premium di default come i corsi. Lezioni ed esercizi di missione
-- ereditano dalla missione (e questa dal corso): nessuna colonna propria.
-- "Missione premium in corso free" non e' ammessa: regola tra due
-- tabelle, garantita dal service.
ALTER TABLE practice_exercises ADD COLUMN IF NOT EXISTS access_tier text NOT NULL DEFAULT 'premium';

ALTER TABLE courses            DROP CONSTRAINT IF EXISTS chk_courses_access_tier_format;
ALTER TABLE missions           DROP CONSTRAINT IF EXISTS chk_missions_access_tier_format;
ALTER TABLE courses            DROP CONSTRAINT IF EXISTS chk_courses_access_tier;
ALTER TABLE courses            ADD  CONSTRAINT chk_courses_access_tier            CHECK (access_tier IN ('free', 'premium'));
ALTER TABLE missions           DROP CONSTRAINT IF EXISTS chk_missions_access_tier;
ALTER TABLE missions           ADD  CONSTRAINT chk_missions_access_tier           CHECK (access_tier IS NULL OR access_tier IN ('free', 'premium'));
ALTER TABLE practice_exercises DROP CONSTRAINT IF EXISTS chk_practice_exercises_access_tier;
ALTER TABLE practice_exercises ADD  CONSTRAINT chk_practice_exercises_access_tier CHECK (access_tier IN ('free', 'premium'));

-- ---------------------------------------------------------------------
-- 3. Versione pubblicata dell'esercizio
-- ---------------------------------------------------------------------
-- L'Admin lavora su nuove versioni in bozza; lo studente svolge solo quella
-- pubblicata. Per cambiare versione, nella stessa transazione si toglie il
-- flag alla vecchia e poi lo si mette alla nuova (l'indice parziale non
-- puo' essere DEFERRABLE).
ALTER TABLE question_versions ADD COLUMN IF NOT EXISTS is_published boolean NOT NULL DEFAULT false;
ALTER TABLE question_versions ADD COLUMN IF NOT EXISTS published_at timestamptz;

ALTER TABLE question_versions DROP CONSTRAINT IF EXISTS chk_question_versions_published;
ALTER TABLE question_versions ADD  CONSTRAINT chk_question_versions_published
  CHECK (NOT is_published OR published_at IS NOT NULL);

CREATE UNIQUE INDEX IF NOT EXISTS uq_question_versions_published
  ON question_versions (question_bank_entry_id) WHERE is_published;

-- Target della FK composta di exercise_attempts
CREATE UNIQUE INDEX IF NOT EXISTS uq_question_versions_id_entry
  ON question_versions (question_version_id, question_bank_entry_id);

-- 3.1 Versione svolta nel tentativo: lo storico non dipende piu' solo dallo
--     snapshot jsonb. NOT NULL: se esistono gia' tentativi la migrazione si
--     interrompe (vanno ricollegati a mano prima di riapplicarla).
ALTER TABLE exercise_attempts ADD COLUMN IF NOT EXISTS question_version_id bigint;
ALTER TABLE exercise_attempts ALTER COLUMN question_version_id SET NOT NULL;

-- Stessa versione dell'esercizio del tentativo
ALTER TABLE exercise_attempts DROP CONSTRAINT IF EXISTS fk_exercise_attempts_version_entry;
ALTER TABLE exercise_attempts ADD  CONSTRAINT fk_exercise_attempts_version_entry
  FOREIGN KEY (question_version_id, question_bank_entry_id)
  REFERENCES question_versions (question_version_id, question_bank_entry_id);

CREATE INDEX IF NOT EXISTS idx_exercise_attempts_version ON exercise_attempts (question_version_id);

-- ---------------------------------------------------------------------
-- 4. Configurazione dell'esercizio di missione
-- ---------------------------------------------------------------------
-- Valori sempre espliciti: il service applica i default di academy_settings
-- alla creazione, cosi' lo storico non dipende da un default che cambia.
-- Il DEFAULT serve solo a riempire le righe esistenti e poi viene tolto.
ALTER TABLE mission_exercises ADD COLUMN IF NOT EXISTS allowed_duration_ms bigint NOT NULL DEFAULT 600000;
ALTER TABLE mission_exercises ALTER COLUMN allowed_duration_ms DROP DEFAULT;
ALTER TABLE mission_exercises ADD COLUMN IF NOT EXISTS max_points numeric(12,2) NOT NULL DEFAULT 10.00;
ALTER TABLE mission_exercises ALTER COLUMN max_points DROP DEFAULT;

ALTER TABLE mission_exercises DROP CONSTRAINT IF EXISTS chk_mission_exercises_allowed_duration;
ALTER TABLE mission_exercises ADD  CONSTRAINT chk_mission_exercises_allowed_duration CHECK (allowed_duration_ms > 0);
ALTER TABLE mission_exercises DROP CONSTRAINT IF EXISTS chk_mission_exercises_max_points;
ALTER TABLE mission_exercises ADD  CONSTRAINT chk_mission_exercises_max_points       CHECK (max_points > 0);

-- 4.1 Ripartizione per skill. La somma dei pesi = 100 coinvolge piu' righe:
--     la garantisce il service (e il controllo "missione pronta" alla
--     pubblicazione). Nessuna riga = 100% sulla skill predefinita della materia.
CREATE TABLE IF NOT EXISTS mission_exercise_skills (
  mission_exercise_id bigint       NOT NULL REFERENCES mission_exercises (mission_exercise_id) DEFERRABLE INITIALLY IMMEDIATE,
  skill_id            bigint       NOT NULL REFERENCES skills (skill_id) DEFERRABLE INITIALLY IMMEDIATE,
  weight              numeric(5,2) NOT NULL,
  PRIMARY KEY (mission_exercise_id, skill_id),
  CONSTRAINT chk_mission_exercise_skills_weight CHECK (weight > 0 AND weight <= 100)
);
CREATE INDEX IF NOT EXISTS idx_mission_exercise_skills_skill ON mission_exercise_skills (skill_id);

-- 4.2 Skill predefinita della materia: fallback dei pesi. NULL = nessun
--     fallback, la missione non si pubblica senza pesi espliciti.
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS default_skill_id bigint;
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS fk_subjects_default_skill;
ALTER TABLE subjects ADD  CONSTRAINT fk_subjects_default_skill
  FOREIGN KEY (default_skill_id) REFERENCES skills (skill_id) DEFERRABLE INITIALLY IMMEDIATE;
CREATE INDEX IF NOT EXISTS idx_subjects_default_skill ON subjects (default_skill_id);

-- ---------------------------------------------------------------------
-- 5. Valutazione della domanda (blueprint §6.2, §6.3)
-- ---------------------------------------------------------------------
-- Sulla domanda, quindi versionata con il contenuto. Scelta multipla e
-- completamento sempre automatici; traduzione di versione sempre docente;
-- traduzione di frase a scelta dell'autrice (alternative accettate in
-- question_answers oppure traduzione libera corretta dalla docente).
ALTER TABLE questions ADD COLUMN IF NOT EXISTS assessment_mode text;
UPDATE questions
   SET assessment_mode = CASE WHEN kind IN ('multiple_choice', 'fill_in') THEN 'automatic' ELSE 'teacher' END
 WHERE assessment_mode IS NULL;
ALTER TABLE questions ALTER COLUMN assessment_mode SET NOT NULL;

ALTER TABLE questions DROP CONSTRAINT IF EXISTS chk_questions_assessment_mode;
ALTER TABLE questions ADD  CONSTRAINT chk_questions_assessment_mode CHECK (
  (kind IN ('multiple_choice', 'fill_in') AND assessment_mode = 'automatic')
  OR (kind = 'passage_translation' AND assessment_mode = 'teacher')
  OR (kind = 'sentence_translation' AND assessment_mode IN ('automatic', 'teacher'))
);

-- 5.1 Rubriche: guida per la docente nella v0.1 (errori strutturali gravi
--     e scelte lessicali contestuali). Nessun punteggio per criterio: la
--     docente assegna percentuale e feedback complessivi. Il legame con le
--     sole traduzioni di versione e' verificato dal service.
CREATE TABLE IF NOT EXISTS question_rubric_criteria (
  question_rubric_criterion_id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  question_id                  bigint  NOT NULL REFERENCES questions (question_id) DEFERRABLE INITIALLY IMMEDIATE,
  sort_order                   integer NOT NULL,
  category                     text    NOT NULL,
  description                  text    NOT NULL,
  CONSTRAINT chk_question_rubric_criteria_sort_order  CHECK (sort_order > 0),
  CONSTRAINT chk_question_rubric_criteria_category    CHECK (category IN ('structural', 'lexical')),
  CONSTRAINT chk_question_rubric_criteria_description CHECK (btrim(description) <> '')
);

-- ---------------------------------------------------------------------
-- 6. Ordinamento con riordino atomico (blueprint §5.1)
-- ---------------------------------------------------------------------
-- Le unicita' esistenti (vincoli *_key o indici unici) non sono DEFERRABLE:
-- si eliminano e si ricreano come vincoli DEFERRABLE INITIALLY IMMEDIATE.
-- Il riordino fa SET CONSTRAINTS <nome> DEFERRED e il controllo avviene al
-- COMMIT. Solo i vincoli di ordinamento: lessons (lesson_id, mission_id) e'
-- target di una FK composta e deve restare non deferrable.
-- Nessuna FK punta a questi vincoli: si possono eliminare e ricreare.
SELECT pg_temp.drop_uniques_on('missions',                 'course_id, sort_order');
SELECT pg_temp.drop_uniques_on('lessons',                  'mission_id, sort_order');
SELECT pg_temp.drop_uniques_on('lesson_pages',             'lesson_id, sort_order');
SELECT pg_temp.drop_uniques_on('question_rubric_criteria', 'question_id, sort_order');

ALTER TABLE missions ADD CONSTRAINT missions_course_id_sort_order_idx
  UNIQUE (course_id, sort_order) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE lessons ADD CONSTRAINT lessons_mission_id_sort_order_idx
  UNIQUE (mission_id, sort_order) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE lesson_pages ADD CONSTRAINT lesson_pages_lesson_id_sort_order_idx
  UNIQUE (lesson_id, sort_order) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE question_rubric_criteria ADD CONSTRAINT question_rubric_criteria_question_id_sort_order_idx
  UNIQUE (question_id, sort_order) DEFERRABLE INITIALLY IMMEDIATE;

-- ---------------------------------------------------------------------
-- 7. Impostazioni dell'Academy (riga singola)
-- ---------------------------------------------------------------------
-- Regole didattiche, separate da gamification_settings. Cambiarle non
-- riscrive lo storico: exercise_assessments.passing_threshold e
-- mission_skill_contributions.factor_snapshot conservano il valore usato.
CREATE TABLE IF NOT EXISTS academy_settings (
  settings_id                 smallint      PRIMARY KEY,
  passing_threshold           numeric(5,2)  NOT NULL DEFAULT 60.00,
  mission_skill_factor        numeric(5,4)  NOT NULL DEFAULT 0.1000,
  default_allowed_duration_ms bigint        NOT NULL DEFAULT 600000,
  default_max_points          numeric(12,2) NOT NULL DEFAULT 10.00,
  updated_at                  timestamptz   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_academy_settings_singleton         CHECK (settings_id = 1),
  CONSTRAINT chk_academy_settings_passing_threshold CHECK (passing_threshold > 0 AND passing_threshold <= 100),
  CONSTRAINT chk_academy_settings_skill_factor      CHECK (mission_skill_factor >= 0 AND mission_skill_factor <= 1),
  CONSTRAINT chk_academy_settings_allowed_duration  CHECK (default_allowed_duration_ms > 0),
  CONSTRAINT chk_academy_settings_max_points        CHECK (default_max_points > 0)
);
INSERT INTO academy_settings (settings_id) VALUES (1) ON CONFLICT (settings_id) DO NOTHING;

COMMIT;
