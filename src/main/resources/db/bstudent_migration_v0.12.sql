-- =====================================================================
-- BStudent - migrazione v0.12 (PostgreSQL)
-- Applica alla v0.11: svolgimento delle missioni (Blocco 5,
-- blueprint/BStudent_Blueprint_Svolgimento_v0.1.md §11).
-- - Lezioni lette nel percorso della missione. Servono a sapere dove si trova
--   lo studente: "Ho finito di leggere" porta al passo successivo, e chi
--   chiude la pagina ritrova la lezione a cui era arrivato. Gli esercizi
--   superati hanno già i loro checkpoint (mission_checkpoints).
--   Un ritentativo dopo un errore usa lo stesso percorso (le lezioni restano
--   lette); abbandonare o ripetere una missione superata apre un percorso nuovo
--   e si riparte da zero.
-- Il Quaderno (notebook_lessons) resta separato: dice quali lezioni lo
-- studente possiede, non dove si trova in una missione.
-- Transazionale e rieseguibile.
-- =====================================================================
BEGIN;

CREATE TABLE IF NOT EXISTS mission_run_lessons (
    mission_run_id bigint NOT NULL REFERENCES mission_runs (mission_run_id),
    lesson_id      bigint NOT NULL REFERENCES lessons (lesson_id),
    read_at        timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (mission_run_id, lesson_id)
);

CREATE INDEX IF NOT EXISTS idx_mission_run_lessons_lesson ON mission_run_lessons (lesson_id);

COMMIT;
