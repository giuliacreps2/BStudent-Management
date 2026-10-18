-- =====================================================================
-- BStudent - migrazione v0.2 (PostgreSQL)
-- Applica al DDL v0.1: Annulla invio, ritiro contenuti, integrita' tra
-- tabelle, coerenza dei giudizi, formula punti Palestra, convenzioni, indici.
-- Transazionale e rieseguibile (se un dato esistente viola un nuovo vincolo,
-- la migrazione si interrompe e non cambia nulla).
-- =====================================================================
BEGIN;

-- Helper temporaneo: elimina i CHECK di una tabella riconosciuti dal testo
-- della definizione, cosi' non dipende dai nomi generati da Postgres.
CREATE FUNCTION pg_temp.drop_checks(p_table regclass, p_pattern text)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  r record;
  n integer := 0;
BEGIN
  FOR r IN
    SELECT conname FROM pg_constraint
    WHERE conrelid = p_table AND contype = 'c'
      AND pg_get_constraintdef(oid) ILIKE p_pattern
  LOOP
    EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', p_table, r.conname);
    n := n + 1;
  END LOOP;
  IF n = 0 THEN
    RAISE NOTICE 'drop_checks: nessun CHECK corrispondente a % su %', p_pattern, p_table;
  ELSIF n > 1 THEN
    RAISE EXCEPTION 'drop_checks: % CHECK corrispondono a % su % (atteso 1)', n, p_pattern, p_table;
  END IF;
END $$;

-- ---------------------------------------------------------------------
-- 1. Convenzioni (blueprint §3)
-- ---------------------------------------------------------------------

-- 1.1 Colonna in italiano -> inglese
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns
             WHERE table_schema = current_schema()
               AND table_name = 'skill_credit_entries'
               AND column_name = 'registrato_il') THEN
    ALTER TABLE skill_credit_entries RENAME COLUMN registrato_il TO recorded_at;
  END IF;
END $$;

-- 1.2 Stati tecnici in snake_case minuscolo, come tutti gli altri stati
UPDATE users          SET status              = lower(status)              WHERE status              <> lower(status);
UPDATE skills         SET status              = lower(status)              WHERE status              <> lower(status);
UPDATE skins          SET status              = lower(status)              WHERE status              <> lower(status);
UPDATE guardian_links SET verification_status = lower(verification_status) WHERE verification_status <> lower(verification_status);
UPDATE auth_tokens    SET purpose             = lower(purpose)             WHERE purpose             <> lower(purpose);

ALTER TABLE users          ALTER COLUMN status              SET DEFAULT 'pending_verification';
ALTER TABLE skills         ALTER COLUMN status              SET DEFAULT 'draft';
ALTER TABLE skins          ALTER COLUMN status              SET DEFAULT 'draft';
ALTER TABLE guardian_links ALTER COLUMN verification_status SET DEFAULT 'pending';

ALTER TABLE users          DROP CONSTRAINT IF EXISTS chk_users_status_format;
ALTER TABLE users          ADD  CONSTRAINT chk_users_status_format          CHECK (status ~ '^[a-z][a-z0-9_]*$');
ALTER TABLE skills         DROP CONSTRAINT IF EXISTS chk_skills_status_format;
ALTER TABLE skills         ADD  CONSTRAINT chk_skills_status_format         CHECK (status ~ '^[a-z][a-z0-9_]*$');
ALTER TABLE skins          DROP CONSTRAINT IF EXISTS chk_skins_status_format;
ALTER TABLE skins          ADD  CONSTRAINT chk_skins_status_format          CHECK (status ~ '^[a-z][a-z0-9_]*$');
ALTER TABLE guardian_links DROP CONSTRAINT IF EXISTS chk_guardian_links_status_format;
ALTER TABLE guardian_links ADD  CONSTRAINT chk_guardian_links_status_format CHECK (verification_status ~ '^[a-z][a-z0-9_]*$');
ALTER TABLE auth_tokens    DROP CONSTRAINT IF EXISTS chk_auth_tokens_purpose_format;
ALTER TABLE auth_tokens    ADD  CONSTRAINT chk_auth_tokens_purpose_format   CHECK (purpose ~ '^[a-z][a-z0-9_]*$');

-- 1.3 Interi coerenti con il resto dello schema
ALTER TABLE student_life_states
  ALTER COLUMN lives                    TYPE integer,
  ALTER COLUMN lives_recovered_in_cycle TYPE integer;

-- ---------------------------------------------------------------------
-- 2. Ritiro dei contenuti (blueprint §5.3): colonna nullable, non impone
--    i tre stati Bozza/Pubblicato/Ritirato, che non sono confermati.
--    NULL = disponibile. Il Taccuino e i cataloghi filtrano in lettura.
-- ---------------------------------------------------------------------
ALTER TABLE courses               ADD COLUMN IF NOT EXISTS withdrawn_at timestamptz;
ALTER TABLE missions              ADD COLUMN IF NOT EXISTS withdrawn_at timestamptz;
ALTER TABLE lessons               ADD COLUMN IF NOT EXISTS withdrawn_at timestamptz;
ALTER TABLE question_bank_entries ADD COLUMN IF NOT EXISTS withdrawn_at timestamptz;
ALTER TABLE practice_exercises    ADD COLUMN IF NOT EXISTS withdrawn_at timestamptz;

-- ---------------------------------------------------------------------
-- 3. "Annulla invio" (blueprint §9.4) e invio in attesa della docente
-- ---------------------------------------------------------------------

-- 3.1 exercise_attempts: nuovo stato terminale 'withdrawn' (esercizio non
--     superato, ma niente fallimento della missione per l'annullamento)
SELECT pg_temp.drop_checks('exercise_attempts', '%saved%');
SELECT pg_temp.drop_checks('exercise_attempts', '%expired%completed_at%');
SELECT pg_temp.drop_checks('exercise_attempts', '%submitted_at%');

ALTER TABLE exercise_attempts ADD CONSTRAINT chk_exercise_attempts_status
  CHECK (status IN ('active','saved','pending_review','assessed','abandoned','expired','withdrawn'));
ALTER TABLE exercise_attempts ADD CONSTRAINT chk_exercise_attempts_completed
  CHECK ((status IN ('assessed','abandoned','expired','withdrawn')) = (completed_at IS NOT NULL));
ALTER TABLE exercise_attempts ADD CONSTRAINT chk_exercise_attempts_submitted
  CHECK (status NOT IN ('pending_review','assessed','withdrawn') OR submitted_at IS NOT NULL);

-- 3.2 mission_runs: stato 'pending_review' (missione consegnata, in attesa
--     della docente). Non conta come "missione in corso".
SELECT pg_temp.drop_checks('mission_runs', '%in_progress%');
ALTER TABLE mission_runs ADD CONSTRAINT chk_mission_runs_status
  CHECK (status IN ('in_progress','pending_review','passed','abandoned'));

-- 3.3 Indici unici parziali (nel DBML esportato il WHERE andava perso)
DROP INDEX IF EXISTS uq_open_mission_run;
CREATE UNIQUE INDEX uq_open_mission_run
  ON mission_runs (student_id) WHERE status = 'in_progress';

DROP INDEX IF EXISTS uq_open_mission_attempt;
CREATE UNIQUE INDEX uq_open_mission_attempt
  ON mission_attempts (mission_run_id) WHERE status IN ('active','saved','pending_review');

DROP INDEX IF EXISTS uq_open_mission_exercise_attempt;
CREATE UNIQUE INDEX uq_open_mission_exercise_attempt
  ON exercise_attempts (mission_attempt_id)
  WHERE context = 'mission' AND status IN ('active','saved','pending_review');

DROP INDEX IF EXISTS uq_open_practice_attempt;
CREATE UNIQUE INDEX uq_open_practice_attempt
  ON exercise_attempts (student_id, question_bank_entry_id)
  WHERE context = 'practice' AND status IN ('active','saved','pending_review');

DROP INDEX IF EXISTS uq_open_life_recovery_attempt;
CREATE UNIQUE INDEX uq_open_life_recovery_attempt
  ON life_recovery_attempts (student_id) WHERE status <> 'completed';

-- ---------------------------------------------------------------------
-- 4. Integrita' tra tabelle (FK composite)
-- ---------------------------------------------------------------------

-- 4.1 mission_attempts conosce lo studente: coincide con quello del percorso
ALTER TABLE mission_attempts ADD COLUMN IF NOT EXISTS student_id bigint;
UPDATE mission_attempts a
   SET student_id = r.student_id
  FROM mission_runs r
 WHERE r.mission_run_id = a.mission_run_id AND a.student_id IS NULL;
ALTER TABLE mission_attempts ALTER COLUMN student_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_mission_attempts_id_student
  ON mission_attempts (mission_attempt_id, student_id);
ALTER TABLE mission_attempts DROP CONSTRAINT IF EXISTS fk_mission_attempts_run_student;
ALTER TABLE mission_attempts ADD CONSTRAINT fk_mission_attempts_run_student
  FOREIGN KEY (mission_run_id, student_id) REFERENCES mission_runs (mission_run_id, student_id);

-- 4.2 exercise_attempts: stesso studente del tentativo di missione, stesso
--     esercizio della voce di missione (le righe Palestra hanno NULL: non verificate)
ALTER TABLE exercise_attempts DROP CONSTRAINT IF EXISTS fk_exercise_attempts_attempt_student;
ALTER TABLE exercise_attempts ADD CONSTRAINT fk_exercise_attempts_attempt_student
  FOREIGN KEY (mission_attempt_id, student_id) REFERENCES mission_attempts (mission_attempt_id, student_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_mission_exercises_id_entry
  ON mission_exercises (mission_exercise_id, question_bank_entry_id);
ALTER TABLE exercise_attempts DROP CONSTRAINT IF EXISTS fk_exercise_attempts_exercise_entry;
ALTER TABLE exercise_attempts ADD CONSTRAINT fk_exercise_attempts_exercise_entry
  FOREIGN KEY (mission_exercise_id, question_bank_entry_id)
  REFERENCES mission_exercises (mission_exercise_id, question_bank_entry_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_exercise_attempts_id_exercise
  ON exercise_attempts (exercise_attempt_id, mission_exercise_id);

-- 4.3 perdite di vita e aiuti: stesso studente del tentativo
ALTER TABLE mission_life_losses DROP CONSTRAINT IF EXISTS fk_mission_life_losses_attempt_student;
ALTER TABLE mission_life_losses ADD CONSTRAINT fk_mission_life_losses_attempt_student
  FOREIGN KEY (mission_attempt_id, student_id) REFERENCES mission_attempts (mission_attempt_id, student_id);

ALTER TABLE hint_uses DROP CONSTRAINT IF EXISTS fk_hint_uses_attempt_student;
ALTER TABLE hint_uses ADD CONSTRAINT fk_hint_uses_attempt_student
  FOREIGN KEY (mission_attempt_id, student_id) REFERENCES mission_attempts (mission_attempt_id, student_id);

-- 4.4 mission_checkpoints: solo esercizi realmente superati, dello stesso
--     percorso, tentativo ed esercizio. 'passed' esiste solo per la FK composita
--     verso exercise_assessments (che richiede passed = true).
ALTER TABLE mission_checkpoints ADD COLUMN IF NOT EXISTS mission_attempt_id bigint;
ALTER TABLE mission_checkpoints ADD COLUMN IF NOT EXISTS passed boolean NOT NULL DEFAULT true;
UPDATE mission_checkpoints c
   SET mission_attempt_id = e.mission_attempt_id
  FROM exercise_attempts e
 WHERE e.exercise_attempt_id = c.exercise_attempt_id AND c.mission_attempt_id IS NULL;
ALTER TABLE mission_checkpoints ALTER COLUMN mission_attempt_id SET NOT NULL;

ALTER TABLE mission_checkpoints DROP CONSTRAINT IF EXISTS chk_mission_checkpoints_passed;
ALTER TABLE mission_checkpoints ADD CONSTRAINT chk_mission_checkpoints_passed CHECK (passed);

CREATE UNIQUE INDEX IF NOT EXISTS uq_exercise_assessments_attempt_passed
  ON exercise_assessments (exercise_attempt_id, passed);

ALTER TABLE mission_checkpoints DROP CONSTRAINT IF EXISTS fk_mission_checkpoints_assessment_passed;
ALTER TABLE mission_checkpoints ADD CONSTRAINT fk_mission_checkpoints_assessment_passed
  FOREIGN KEY (exercise_attempt_id, passed) REFERENCES exercise_assessments (exercise_attempt_id, passed);

ALTER TABLE mission_checkpoints DROP CONSTRAINT IF EXISTS fk_mission_checkpoints_attempt_run;
ALTER TABLE mission_checkpoints ADD CONSTRAINT fk_mission_checkpoints_attempt_run
  FOREIGN KEY (mission_attempt_id, mission_run_id) REFERENCES mission_attempts (mission_attempt_id, mission_run_id);

ALTER TABLE mission_checkpoints DROP CONSTRAINT IF EXISTS fk_mission_checkpoints_exercise_attempt;
ALTER TABLE mission_checkpoints ADD CONSTRAINT fk_mission_checkpoints_exercise_attempt
  FOREIGN KEY (exercise_attempt_id, mission_attempt_id)
  REFERENCES exercise_attempts (exercise_attempt_id, mission_attempt_id);

ALTER TABLE mission_checkpoints DROP CONSTRAINT IF EXISTS fk_mission_checkpoints_exercise_item;
ALTER TABLE mission_checkpoints ADD CONSTRAINT fk_mission_checkpoints_exercise_item
  FOREIGN KEY (exercise_attempt_id, mission_exercise_id)
  REFERENCES exercise_attempts (exercise_attempt_id, mission_exercise_id);

-- ---------------------------------------------------------------------
-- 5. Giudizi e punti
-- ---------------------------------------------------------------------

-- 5.1 Giudizio automatico: passed e' sempre derivato da percentuale e soglia.
--     Per i giudizi docente resta libero (es. rielaborazione: supera il completamento).
ALTER TABLE exercise_assessments DROP CONSTRAINT IF EXISTS chk_exercise_assessments_passed;
ALTER TABLE exercise_assessments ADD CONSTRAINT chk_exercise_assessments_passed
  CHECK (assessment_method <> 'automatic'
         OR (passed IS NOT NULL AND passed = (percentage >= passing_threshold)));

-- 5.2 Formula confermata (§10.1): punti = massimo x percentuale / 100, 2 decimali
ALTER TABLE practice_records DROP CONSTRAINT IF EXISTS chk_practice_records_points_formula;
ALTER TABLE practice_records ADD CONSTRAINT chk_practice_records_points_formula
  CHECK (points = round(max_points_snapshot * percentage / 100, 2));

-- 5.3 Un giudizio alimenta un solo record
CREATE UNIQUE INDEX IF NOT EXISTS uq_practice_records_attempt ON practice_records (exercise_attempt_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_mission_records_attempt  ON mission_records  (mission_attempt_id);

-- ---------------------------------------------------------------------
-- 6. Indici sulle FK (Postgres non li crea) e sullo storico
-- ---------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_courses_subject                 ON courses (subject_id);
CREATE INDEX IF NOT EXISTS idx_mission_exercises_entry         ON mission_exercises (question_bank_entry_id);
CREATE INDEX IF NOT EXISTS idx_practice_exercises_skill        ON practice_exercises (skill_id);
CREATE INDEX IF NOT EXISTS idx_practice_exercises_topic        ON practice_exercises (topic_id);
CREATE INDEX IF NOT EXISTS idx_accessories_skill               ON accessories (skill_id);
CREATE INDEX IF NOT EXISTS idx_student_accessories_accessory   ON student_accessories (accessory_id);
CREATE INDEX IF NOT EXISTS idx_equipped_accessories_accessory  ON equipped_accessories (accessory_id, slot_code);
CREATE INDEX IF NOT EXISTS idx_hint_uses_attempt               ON hint_uses (mission_attempt_id, student_id);
CREATE INDEX IF NOT EXISTS idx_hint_uses_accessory             ON hint_uses (student_id, accessory_id);
CREATE INDEX IF NOT EXISTS idx_mission_life_losses_student     ON mission_life_losses (student_id);
CREATE INDEX IF NOT EXISTS idx_mission_life_losses_attempt     ON mission_life_losses (mission_attempt_id, student_id);
CREATE INDEX IF NOT EXISTS idx_life_recovery_attempts_activity ON life_recovery_attempts (activity_id);
CREATE INDEX IF NOT EXISTS idx_mission_runs_mission            ON mission_runs (mission_id);
CREATE INDEX IF NOT EXISTS idx_exercise_attempts_entry         ON exercise_attempts (question_bank_entry_id);
CREATE INDEX IF NOT EXISTS idx_exercise_attempts_item          ON exercise_attempts (mission_exercise_id);
CREATE INDEX IF NOT EXISTS idx_exercise_attempts_history
  ON exercise_attempts (student_id, question_bank_entry_id, started_at DESC);
CREATE INDEX IF NOT EXISTS idx_exercise_assessments_teacher    ON exercise_assessments (teacher_user_id);
CREATE INDEX IF NOT EXISTS idx_reformulation_reviews_teacher   ON content_reformulation_reviews (teacher_user_id);
CREATE INDEX IF NOT EXISTS idx_mission_checkpoints_item        ON mission_checkpoints (mission_exercise_id);
CREATE INDEX IF NOT EXISTS idx_mission_checkpoints_attempt     ON mission_checkpoints (mission_attempt_id);
CREATE INDEX IF NOT EXISTS idx_practice_records_exercise       ON practice_records (practice_exercise_id);
CREATE INDEX IF NOT EXISTS idx_practice_records_skill          ON practice_records (skill_id);
CREATE INDEX IF NOT EXISTS idx_mission_skill_contrib_mission   ON mission_skill_contributions (mission_id);
CREATE INDEX IF NOT EXISTS idx_mission_skill_contrib_skill     ON mission_skill_contributions (skill_id);
CREATE INDEX IF NOT EXISTS idx_mission_skill_contrib_attempt   ON mission_skill_contributions (mission_attempt_id);
CREATE INDEX IF NOT EXISTS idx_skill_credit_entries_student    ON skill_credit_entries (student_id, skill_id);
CREATE INDEX IF NOT EXISTS idx_skill_credit_entries_skill      ON skill_credit_entries (skill_id);
CREATE INDEX IF NOT EXISTS idx_mission_records_mission         ON mission_records (mission_id);
CREATE INDEX IF NOT EXISTS idx_course_results_course           ON course_results (course_id);
CREATE INDEX IF NOT EXISTS idx_first_evolutions_course         ON student_first_evolutions (course_id);
CREATE INDEX IF NOT EXISTS idx_first_evolutions_skin           ON student_first_evolutions (skin_id);
CREATE INDEX IF NOT EXISTS idx_notebook_lessons_lesson         ON notebook_lessons (lesson_id);
CREATE INDEX IF NOT EXISTS idx_lesson_notes_lesson             ON lesson_notes (lesson_id);
CREATE INDEX IF NOT EXISTS idx_orientation_recs_exercise       ON orientation_recommendations (practice_exercise_id);

COMMIT;
