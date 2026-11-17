-- =====================================================================
-- BStudent - migrazione v0.11 (PostgreSQL)
-- Applica alla v0.10: svolgimento delle missioni (Blocco 5,
-- blueprint/BStudent_Blueprint_Svolgimento_v0.1.md §9, deciso il 17/11/2026).
-- - Una missione in corso PER MATERIA (prima: una per studente). Lo studente
--   puo' avere aperte insieme una missione di Latino e una di Greco.
-- La materia si ricava dalla missione (missione -> corso -> materia) ma si
-- tiene anche sul percorso, perche' un indice unico non puo' leggere altre
-- tabelle.
-- Transazionale e rieseguibile.
-- =====================================================================
BEGIN;

-- ---------------------------------------------------------------------
-- 1. Materia sul percorso della missione
-- ---------------------------------------------------------------------
ALTER TABLE mission_runs ADD COLUMN IF NOT EXISTS subject_id bigint REFERENCES subjects (subject_id);

-- Percorsi esistenti: materia dalla missione (oggi non ce ne sono, ma la
-- migrazione deve funzionare anche su un database con dati)
UPDATE mission_runs r
   SET subject_id = c.subject_id
  FROM missions m
  JOIN courses c ON c.course_id = m.course_id
 WHERE m.mission_id = r.mission_id
   AND r.subject_id IS NULL;

ALTER TABLE mission_runs ALTER COLUMN subject_id SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_mission_runs_subject ON mission_runs (subject_id);

-- ---------------------------------------------------------------------
-- 2. Una missione in corso per studente e materia
-- ---------------------------------------------------------------------
-- Una missione in attesa della docente (pending_review) non conta come
-- "in corso" (Svolgimento §1.4): l'indice guarda solo in_progress.
DROP INDEX IF EXISTS uq_open_mission_run;
CREATE UNIQUE INDEX uq_open_mission_run
  ON mission_runs (student_id, subject_id) WHERE status = 'in_progress';

COMMIT;
