-- =====================================================================
-- BStudent - migrazione v0.7 (PostgreSQL)
-- Applica alla v0.6: date editoriali per l'area Admin.
-- - created_at / updated_at su corsi, missioni, lezioni, esercizi e versioni;
-- - replaced_at sulle versioni: quando una versione smette di essere quella
--   pubblicata, cosi' ogni versione mostra "in uso dal ... al ...".
-- Le righe esistenti non hanno una data di creazione nota: ricevono il
-- momento della migrazione, anticipato alla prima pubblicazione se precedente.
-- Transazionale e rieseguibile.
-- =====================================================================
BEGIN;

-- ---------------------------------------------------------------------
-- 1. Creazione e ultima modifica
-- ---------------------------------------------------------------------
-- I DEFAULT restano: valorizzano anche gli INSERT fatti a mano in SQL.
-- L'applicazione li imposta comunque (@CreationTimestamp / @UpdateTimestamp)
DO $$
DECLARE
  t text;
BEGIN
  FOREACH t IN ARRAY ARRAY['courses', 'missions', 'lessons', 'question_bank_entries', 'question_versions']
  LOOP
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS created_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP', t);
    EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP', t);
    -- Un contenuto non puo' essere creato dopo la sua prima pubblicazione
    EXECUTE format('UPDATE %I SET created_at = published_at WHERE published_at IS NOT NULL AND created_at > published_at', t);
  END LOOP;
END $$;

-- ---------------------------------------------------------------------
-- 2. Fine dell'uso di una versione
-- ---------------------------------------------------------------------
ALTER TABLE question_versions ADD COLUMN IF NOT EXISTS replaced_at timestamptz;

-- Versioni gia' sostituite prima di questa migrazione: la data vera non e' nota
UPDATE question_versions
   SET replaced_at = CURRENT_TIMESTAMP
 WHERE NOT is_published AND published_at IS NOT NULL AND replaced_at IS NULL;

-- Solo una versione pubblicata in passato e oggi non piu' in uso ha replaced_at
ALTER TABLE question_versions DROP CONSTRAINT IF EXISTS chk_question_versions_replaced;
ALTER TABLE question_versions ADD  CONSTRAINT chk_question_versions_replaced
  CHECK (replaced_at IS NULL OR (NOT is_published AND published_at IS NOT NULL));

COMMIT;
