-- =====================================================================
-- BStudent - migrazione v0.9 (PostgreSQL)
-- Applica alla v0.8: campi chiesti dal frontend (mappa del Blocco 4,
-- blueprint §5.8, §5.9, §6.1, §7.3, §7.4, §8, §10.4-ter, §10.5, §14).
-- - materie: slug, tagline, immagine, stato, guida del briefing;
-- - corsi: slug, tagline, livello, "in evidenza", tema del mondo, citazione;
-- - missioni: slug, tagline, copertina, prova finale, briefing, obiettivi;
-- - banco: titolo per lo studente e argomento; lezioni: argomento;
-- - Palestra: immagine dell'argomento, tempo stimato;
-- - skill: icona, colore e skill di ogni materia;
-- - test: tipo (orientamento/livello), livello delle opzioni, risultati;
-- - Risorse, collegamenti (solo struttura) e segnalibri dello studente;
-- - registro di studio (eventi), tutorial visto, colore del Quaderno.
-- Fuori: Blog (v0.10) e gamification (branch dedicato).
-- Transazionale e rieseguibile.
-- =====================================================================
BEGIN;

-- Helper temporanei ---------------------------------------------------

-- Slug da un testo: minuscole, senza accenti, parole unite da "-"
CREATE FUNCTION pg_temp.slugify(p text) RETURNS text LANGUAGE sql IMMUTABLE AS $$
  SELECT coalesce(nullif(btrim(regexp_replace(
           translate(lower(p), 'àáâäãèéêëìíîïòóôöõùúûüçñ', 'aaaaaeeeeiiiiooooouuuucn'),
           '[^a-z0-9]+', '-', 'g'), '-'), ''), 'x')
$$;

-- Come in v0.6: elimina ogni unicita' esistente su esattamente quelle colonne
CREATE FUNCTION pg_temp.drop_uniques_on(p_table regclass, p_columns text)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE
  r record;
BEGIN
  FOR r IN
    SELECT conname FROM pg_constraint
    WHERE conrelid = p_table AND contype = 'u'
      AND pg_get_constraintdef(oid) LIKE format('UNIQUE (%s)%%', p_columns)
  LOOP
    EXECUTE format('ALTER TABLE %s DROP CONSTRAINT %I', p_table, r.conname);
  END LOOP;
END $$;

-- ---------------------------------------------------------------------
-- 1. Materie (§5.8, §5.9)
-- ---------------------------------------------------------------------
-- Stato: draft (nascosta) / published. Pubblicata senza corsi pubblicati =
-- "Presto disponibile", calcolato nel service. Le materie esistenti erano
-- gia' in uso: partono published; le nuove nascono draft.
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS status text NOT NULL DEFAULT 'published';
ALTER TABLE subjects ALTER COLUMN status SET DEFAULT 'draft';
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS published_at timestamptz;
UPDATE subjects SET published_at = CURRENT_TIMESTAMP WHERE status = 'published' AND published_at IS NULL;

ALTER TABLE subjects ADD COLUMN IF NOT EXISTS slug            text;
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS tagline         text;
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS image_reference text;
-- Guida fissa della materia che presenta le missioni (briefing)
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS guide_name            text;
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS guide_image_reference text;

UPDATE subjects SET slug = pg_temp.slugify(name) WHERE slug IS NULL;
UPDATE subjects s SET slug = s.slug || '-' || s.subject_id
 WHERE EXISTS (SELECT 1 FROM subjects o WHERE o.slug = s.slug AND o.subject_id < s.subject_id);
ALTER TABLE subjects ALTER COLUMN slug SET NOT NULL;

ALTER TABLE subjects DROP CONSTRAINT IF EXISTS chk_subjects_status;
ALTER TABLE subjects ADD  CONSTRAINT chk_subjects_status    CHECK (status IN ('draft', 'published'));
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS chk_subjects_published;
ALTER TABLE subjects ADD  CONSTRAINT chk_subjects_published CHECK (status <> 'published' OR published_at IS NOT NULL);
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS chk_subjects_slug;
ALTER TABLE subjects ADD  CONSTRAINT chk_subjects_slug      CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$');
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS uq_subjects_slug;
ALTER TABLE subjects ADD  CONSTRAINT uq_subjects_slug       UNIQUE (slug);

-- ---------------------------------------------------------------------
-- 2. Corsi (§5.8, §5.9, §8.2)
-- ---------------------------------------------------------------------
ALTER TABLE courses ADD COLUMN IF NOT EXISTS slug    text;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS tagline text;
-- Facoltativo nel DB: obbligatorio per pubblicare (service), i corsi esistenti non lo hanno
ALTER TABLE courses ADD COLUMN IF NOT EXISTS level text;
-- "In evidenza" scelto dall'Admin fino a questa data (durata suggerita: una settimana)
ALTER TABLE courses ADD COLUMN IF NOT EXISTS featured_until timestamptz;
-- Tema della mappa: le illustrazioni e i posti delle missioni stanno nel frontend
ALTER TABLE courses ADD COLUMN IF NOT EXISTS world_theme  text;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS quote        text;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS quote_author text;

UPDATE courses SET slug = pg_temp.slugify(title) WHERE slug IS NULL;
UPDATE courses c SET slug = c.slug || '-' || c.course_id
 WHERE EXISTS (SELECT 1 FROM courses o WHERE o.slug = c.slug AND o.course_id < c.course_id);
ALTER TABLE courses ALTER COLUMN slug SET NOT NULL;

ALTER TABLE courses DROP CONSTRAINT IF EXISTS chk_courses_slug;
ALTER TABLE courses ADD  CONSTRAINT chk_courses_slug        CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$');
ALTER TABLE courses DROP CONSTRAINT IF EXISTS uq_courses_slug;
ALTER TABLE courses ADD  CONSTRAINT uq_courses_slug         UNIQUE (slug);
ALTER TABLE courses DROP CONSTRAINT IF EXISTS chk_courses_level;
ALTER TABLE courses ADD  CONSTRAINT chk_courses_level       CHECK (level IS NULL OR level IN ('beginner', 'intermediate', 'advanced'));
ALTER TABLE courses DROP CONSTRAINT IF EXISTS chk_courses_world_theme;
ALTER TABLE courses ADD  CONSTRAINT chk_courses_world_theme CHECK (world_theme IS NULL OR world_theme ~ '^[a-z0-9]+(-[a-z0-9]+)*$');
-- Autore solo insieme alla citazione
ALTER TABLE courses DROP CONSTRAINT IF EXISTS chk_courses_quote;
ALTER TABLE courses ADD  CONSTRAINT chk_courses_quote       CHECK (quote_author IS NULL OR quote IS NOT NULL);

-- ---------------------------------------------------------------------
-- 3. Missioni (§5.9)
-- ---------------------------------------------------------------------
-- Slug unico nel corso: l'ordine non va bene come indirizzo, cambia col riordino
ALTER TABLE missions ADD COLUMN IF NOT EXISTS slug            text;
ALTER TABLE missions ADD COLUMN IF NOT EXISTS tagline         text;
ALTER TABLE missions ADD COLUMN IF NOT EXISTS cover_reference text;
ALTER TABLE missions ADD COLUMN IF NOT EXISTS is_final        boolean NOT NULL DEFAULT false;
-- Video facoltativo del briefing, come le pagine video della lezione
ALTER TABLE missions ADD COLUMN IF NOT EXISTS briefing_video_provider   text;
ALTER TABLE missions ADD COLUMN IF NOT EXISTS briefing_video_identifier text;

UPDATE missions SET slug = pg_temp.slugify(title) WHERE slug IS NULL;
UPDATE missions m SET slug = m.slug || '-' || m.mission_id
 WHERE EXISTS (SELECT 1 FROM missions o
                WHERE o.course_id = m.course_id AND o.slug = m.slug AND o.mission_id < m.mission_id);
ALTER TABLE missions ALTER COLUMN slug SET NOT NULL;

ALTER TABLE missions DROP CONSTRAINT IF EXISTS chk_missions_slug;
ALTER TABLE missions ADD  CONSTRAINT chk_missions_slug  CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$');
ALTER TABLE missions DROP CONSTRAINT IF EXISTS uq_missions_course_slug;
ALTER TABLE missions ADD  CONSTRAINT uq_missions_course_slug UNIQUE (course_id, slug);
ALTER TABLE missions DROP CONSTRAINT IF EXISTS chk_missions_briefing_video;
ALTER TABLE missions ADD  CONSTRAINT chk_missions_briefing_video
  CHECK ((briefing_video_provider IS NULL) = (briefing_video_identifier IS NULL));

-- Al massimo una prova finale per corso; non e' per forza l'ultima missione
CREATE UNIQUE INDEX IF NOT EXISTS uq_missions_final_per_course ON missions (course_id) WHERE is_final;

-- 3.1 Battute della guida nel briefing: da 1 a 5 (limite nel service)
CREATE TABLE IF NOT EXISTS mission_briefing_lines (
  mission_briefing_line_id bigint  GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  mission_id               bigint  NOT NULL REFERENCES missions (mission_id) DEFERRABLE INITIALLY IMMEDIATE,
  sort_order               integer NOT NULL,
  text                     text    NOT NULL,
  CONSTRAINT chk_mission_briefing_lines_sort_order CHECK (sort_order > 0),
  CONSTRAINT chk_mission_briefing_lines_text       CHECK (btrim(text) <> ''),
  CONSTRAINT mission_briefing_lines_mission_id_sort_order_idx
    UNIQUE (mission_id, sort_order) DEFERRABLE INITIALLY IMMEDIATE
);

-- 3.2 "Cosa imparerai": massimo 5 frasi brevi (limite nel service)
CREATE TABLE IF NOT EXISTS mission_learning_goals (
  mission_learning_goal_id bigint  GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  mission_id               bigint  NOT NULL REFERENCES missions (mission_id) DEFERRABLE INITIALLY IMMEDIATE,
  sort_order               integer NOT NULL,
  text                     text    NOT NULL,
  CONSTRAINT chk_mission_learning_goals_sort_order CHECK (sort_order > 0),
  CONSTRAINT chk_mission_learning_goals_text       CHECK (btrim(text) <> ''),
  CONSTRAINT mission_learning_goals_mission_id_sort_order_idx
    UNIQUE (mission_id, sort_order) DEFERRABLE INITIALLY IMMEDIATE
);

-- ---------------------------------------------------------------------
-- 4. Argomenti, banco, lezioni, Palestra (§6.1, §7.3, §10.5)
-- ---------------------------------------------------------------------
ALTER TABLE topics ADD COLUMN IF NOT EXISTS image_reference text;

-- 4.1 Titolo per lo studente: sulla scheda, non sulla versione (correggerlo
--     non crea una versione). Obbligatorio per pubblicare. Gli esercizi
--     esistenti ricevono il titolo interno.
ALTER TABLE question_bank_entries ADD COLUMN IF NOT EXISTS title text;
UPDATE question_bank_entries SET title = internal_title WHERE title IS NULL;
ALTER TABLE question_bank_entries DROP CONSTRAINT IF EXISTS chk_question_bank_entries_title;
ALTER TABLE question_bank_entries ADD  CONSTRAINT chk_question_bank_entries_title
  CHECK (CASE WHEN title IS NULL THEN status = 'draft' ELSE btrim(title) <> '' END);

-- 4.2 Argomento sull'esercizio e sulla lezione: collega esercizio -> argomento
--     -> teoria (registro di studio, "rileggi la teoria"). Facoltativo.
--     Stessa materia del contenuto: regola tra tabelle, nel service.
ALTER TABLE question_bank_entries ADD COLUMN IF NOT EXISTS topic_id bigint;
ALTER TABLE question_bank_entries DROP CONSTRAINT IF EXISTS fk_question_bank_entries_topic;
ALTER TABLE question_bank_entries ADD  CONSTRAINT fk_question_bank_entries_topic
  FOREIGN KEY (topic_id) REFERENCES topics (topic_id) DEFERRABLE INITIALLY IMMEDIATE;
CREATE INDEX IF NOT EXISTS idx_question_bank_entries_topic ON question_bank_entries (topic_id);

ALTER TABLE lessons ADD COLUMN IF NOT EXISTS topic_id bigint;
ALTER TABLE lessons DROP CONSTRAINT IF EXISTS fk_lessons_topic;
ALTER TABLE lessons ADD  CONSTRAINT fk_lessons_topic
  FOREIGN KEY (topic_id) REFERENCES topics (topic_id) DEFERRABLE INITIALLY IMMEDIATE;
CREATE INDEX IF NOT EXISTS idx_lessons_topic ON lessons (topic_id);

-- 4.3 Tempo stimato della voce Palestra: solo informativo, non e' un timer
ALTER TABLE practice_exercises ADD COLUMN IF NOT EXISTS estimated_minutes smallint;
ALTER TABLE practice_exercises DROP CONSTRAINT IF EXISTS chk_practice_exercises_estimated_minutes;
ALTER TABLE practice_exercises ADD  CONSTRAINT chk_practice_exercises_estimated_minutes
  CHECK (estimated_minutes IS NULL OR estimated_minutes > 0);

-- ---------------------------------------------------------------------
-- 5. Skill (§10.4-ter)
-- ---------------------------------------------------------------------
-- Icona e colore scelti nell'Admin da un elenco fisso del frontend: una
-- skill nuova non richiede modifiche al codice. Solo il formato e' vincolato.
ALTER TABLE skills ADD COLUMN IF NOT EXISTS icon  text;
ALTER TABLE skills ADD COLUMN IF NOT EXISTS color text;
ALTER TABLE skills DROP CONSTRAINT IF EXISTS chk_skills_icon;
ALTER TABLE skills ADD  CONSTRAINT chk_skills_icon  CHECK (icon  IS NULL OR icon  ~ '^[a-z0-9]+(-[a-z0-9]+)*$');
ALTER TABLE skills DROP CONSTRAINT IF EXISTS chk_skills_color;
ALTER TABLE skills ADD  CONSTRAINT chk_skills_color CHECK (color IS NULL OR color ~ '^[a-z0-9]+(-[a-z0-9]+)*$');

-- 5.1 Skill scelte da ogni materia, nell'ordine dell'autrice
CREATE TABLE IF NOT EXISTS subject_skills (
  subject_id bigint  NOT NULL REFERENCES subjects (subject_id) DEFERRABLE INITIALLY IMMEDIATE,
  skill_id   bigint  NOT NULL REFERENCES skills (skill_id) DEFERRABLE INITIALLY IMMEDIATE,
  sort_order integer NOT NULL,
  PRIMARY KEY (subject_id, skill_id),
  CONSTRAINT chk_subject_skills_sort_order CHECK (sort_order > 0),
  CONSTRAINT subject_skills_subject_id_sort_order_idx
    UNIQUE (subject_id, sort_order) DEFERRABLE INITIALLY IMMEDIATE
);
CREATE INDEX IF NOT EXISTS idx_subject_skills_skill ON subject_skills (skill_id);

-- Dati esistenti: ogni materia riceve la sua skill predefinita e le skill
-- gia' usate dai suoi esercizi (Palestra e missioni). Solo se la tabella e' vuota.
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM subject_skills) THEN
    INSERT INTO subject_skills (subject_id, skill_id, sort_order)
    SELECT subject_id, skill_id, row_number() OVER (PARTITION BY subject_id ORDER BY skill_id)
      FROM (SELECT subject_id, default_skill_id AS skill_id FROM subjects WHERE default_skill_id IS NOT NULL
            UNION
            SELECT t.subject_id, pe.skill_id
              FROM practice_exercises pe JOIN topics t ON t.topic_id = pe.topic_id
            UNION
            SELECT c.subject_id, mes.skill_id
              FROM mission_exercise_skills mes
              JOIN mission_exercises me ON me.mission_exercise_id = mes.mission_exercise_id
              JOIN missions m ON m.mission_id = me.mission_id
              JOIN courses c ON c.course_id = m.course_id) s;
  END IF;
END $$;

-- ---------------------------------------------------------------------
-- 6. Test di orientamento e di livello (§8)
-- ---------------------------------------------------------------------
-- Un test per materia e per tipo, al posto di uno solo per materia
ALTER TABLE orientation_tests ADD COLUMN IF NOT EXISTS kind text NOT NULL DEFAULT 'exercises';
ALTER TABLE orientation_tests DROP CONSTRAINT IF EXISTS chk_orientation_tests_kind;
ALTER TABLE orientation_tests ADD  CONSTRAINT chk_orientation_tests_kind CHECK (kind IN ('exercises', 'course_level'));
SELECT pg_temp.drop_uniques_on('orientation_tests', 'subject_id');
ALTER TABLE orientation_tests DROP CONSTRAINT IF EXISTS uq_orientation_tests_subject_kind;
ALTER TABLE orientation_tests ADD  CONSTRAINT uq_orientation_tests_subject_kind UNIQUE (subject_id, kind);

-- Livello dell'opzione: obbligatorio solo nei test course_level, e le
-- associazioni agli esercizi valgono solo nei test exercises (service).
ALTER TABLE orientation_options ADD COLUMN IF NOT EXISTS level text;
ALTER TABLE orientation_options DROP CONSTRAINT IF EXISTS chk_orientation_options_level;
ALTER TABLE orientation_options ADD  CONSTRAINT chk_orientation_options_level
  CHECK (level IS NULL OR level IN ('beginner', 'intermediate', 'advanced'));

-- 6.1 Risultati salvati: fotografia, non ricalcolata se l'autrice cambia il test.
--     answers = {"<questionId>": <optionId>, ...}. Esattamente un risultato
--     secondo il tipo del test (verificato dal service).
CREATE TABLE IF NOT EXISTS orientation_results (
  orientation_result_id             bigint      GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  student_id                        bigint      NOT NULL REFERENCES students (student_id) ON DELETE CASCADE,
  orientation_test_id               bigint      NOT NULL REFERENCES orientation_tests (orientation_test_id),
  answers                           jsonb       NOT NULL,
  -- Test course_level: livello consigliato
  result_level                      text,
  -- Test exercises: voci consigliate in ordine (vuoto = nessun consiglio)
  recommended_practice_exercise_ids bigint[],
  created_at                        timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_orientation_results_answers CHECK (jsonb_typeof(answers) = 'object'),
  CONSTRAINT chk_orientation_results_level
    CHECK (result_level IS NULL OR result_level IN ('beginner', 'intermediate', 'advanced')),
  CONSTRAINT chk_orientation_results_one_result
    CHECK (num_nonnulls(result_level, recommended_practice_exercise_ids) = 1)
);
CREATE INDEX IF NOT EXISTS idx_orientation_results_student
  ON orientation_results (student_id, orientation_test_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orientation_results_test ON orientation_results (orientation_test_id);

-- ---------------------------------------------------------------------
-- 7. Risorse (§7.4)
-- ---------------------------------------------------------------------
-- Stati e date come gli altri contenuti (v0.6, v0.7). Le parti che si
-- leggono intere (tabelle, approfondimenti a blocchi, esempi) stanno in
-- content: la forma la controlla il backend.
CREATE TABLE IF NOT EXISTS resources (
  resource_id     bigint      GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  subject_id      bigint      NOT NULL REFERENCES subjects (subject_id) DEFERRABLE INITIALLY IMMEDIATE,
  -- Elenco fisso, separato dalle skill
  category        text        NOT NULL,
  slug            text        NOT NULL,
  title           text        NOT NULL,
  description     text        NOT NULL,
  -- "In breve": testo breve e mini esempio facoltativo
  summary_text    text,
  summary_example text,
  cover_reference text,
  content         jsonb       NOT NULL DEFAULT '{}'::jsonb,
  -- Argomento della Palestra per "Vai agli esercizi"
  topic_id        bigint      REFERENCES topics (topic_id) DEFERRABLE INITIALLY IMMEDIATE,
  access_tier     text        NOT NULL DEFAULT 'premium',
  -- "Le risorse piu' usate": scelte dall'autrice finche' non c'e' l'uso reale
  is_most_used    boolean     NOT NULL DEFAULT false,
  most_used_icon  text,
  is_featured     boolean     NOT NULL DEFAULT false,
  status          text        NOT NULL DEFAULT 'draft',
  published_at    timestamptz,
  withdrawn_at    timestamptz,
  created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_resources_subject_slug  UNIQUE (subject_id, slug),
  CONSTRAINT chk_resources_category     CHECK (category IN ('grammatica', 'verbi', 'traduzione', 'vocabolario')),
  CONSTRAINT chk_resources_slug         CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  CONSTRAINT chk_resources_title        CHECK (btrim(title) <> ''),
  CONSTRAINT chk_resources_content      CHECK (jsonb_typeof(content) = 'object'),
  CONSTRAINT chk_resources_access_tier  CHECK (access_tier IN ('free', 'premium')),
  CONSTRAINT chk_resources_most_used    CHECK (is_most_used = (most_used_icon IS NOT NULL)),
  CONSTRAINT chk_resources_icon         CHECK (most_used_icon IS NULL OR most_used_icon ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
  CONSTRAINT chk_resources_status       CHECK (status IN ('draft', 'published', 'withdrawn')),
  CONSTRAINT chk_resources_withdrawn    CHECK ((status = 'withdrawn') = (withdrawn_at IS NOT NULL)),
  CONSTRAINT chk_resources_published    CHECK (status <> 'published' OR published_at IS NOT NULL)
);
CREATE INDEX IF NOT EXISTS idx_resources_topic ON resources (topic_id);

-- 7.1 Collegamenti "Approfondisci": solo struttura, nessun Admin ne' lettura
--     nell'MVP. origin: scelto dall'autrice o calcolato da un algoritmo.
--     Verso gli articoli del Blog: si aggiunge con la v0.10.
CREATE TABLE IF NOT EXISTS resource_links (
  resource_link_id   bigint      GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  resource_id        bigint      NOT NULL REFERENCES resources (resource_id) DEFERRABLE INITIALLY IMMEDIATE,
  target_resource_id bigint      REFERENCES resources (resource_id) DEFERRABLE INITIALLY IMMEDIATE,
  target_topic_id    bigint      REFERENCES topics (topic_id) DEFERRABLE INITIALLY IMMEDIATE,
  sort_order         integer     NOT NULL,
  origin             text        NOT NULL DEFAULT 'manual',
  created_at         timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_resource_links_target     CHECK (num_nonnulls(target_resource_id, target_topic_id) = 1),
  CONSTRAINT chk_resource_links_not_self   CHECK (target_resource_id IS NULL OR target_resource_id <> resource_id),
  CONSTRAINT chk_resource_links_sort_order CHECK (sort_order > 0),
  CONSTRAINT chk_resource_links_origin     CHECK (origin IN ('manual', 'automatic')),
  CONSTRAINT resource_links_resource_id_sort_order_idx
    UNIQUE (resource_id, sort_order) DEFERRABLE INITIALLY IMMEDIATE
);
CREATE INDEX IF NOT EXISTS idx_resource_links_target_resource ON resource_links (target_resource_id);
CREATE INDEX IF NOT EXISTS idx_resource_links_target_topic    ON resource_links (target_topic_id);

-- 7.2 Segnalibri del Quaderno: la Risorsa salvata e' un collegamento,
--     non un contenuto del Quaderno (§14)
CREATE TABLE IF NOT EXISTS resource_bookmarks (
  student_id  bigint      NOT NULL REFERENCES students (student_id) ON DELETE CASCADE,
  resource_id bigint      NOT NULL REFERENCES resources (resource_id),
  saved_at    timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (student_id, resource_id)
);
CREATE INDEX IF NOT EXISTS idx_resource_bookmarks_resource ON resource_bookmarks (resource_id);

-- ---------------------------------------------------------------------
-- 8. Studente: tutorial e Quaderno (§5.9, §14)
-- ---------------------------------------------------------------------
-- Tutorial della prima volta: mostrato una sola volta
ALTER TABLE students ADD COLUMN IF NOT EXISTS tutorial_seen_at timestamptz;
-- Colore della copertina dalla tavolozza del frontend; NULL = predefinito
ALTER TABLE students ADD COLUMN IF NOT EXISTS notebook_cover_color text;
ALTER TABLE students DROP CONSTRAINT IF EXISTS chk_students_notebook_cover_color;
ALTER TABLE students ADD  CONSTRAINT chk_students_notebook_cover_color
  CHECK (notebook_cover_color IS NULL OR notebook_cover_color ~ '^[a-z0-9]+(-[a-z0-9]+)*$');

-- ---------------------------------------------------------------------
-- 9. Registro di studio (§10.5)
-- ---------------------------------------------------------------------
-- Si aggiunge e non si modifica. I tentativi hanno gia' i loro orari.
-- Teoria: il frontend somma il tempo attivo (segnale ogni 30 s, stop dopo
-- 10 min senza interazione o con la scheda nascosta) e lo invia a tratti:
-- una riga per tratto, con inizio e millisecondi attivi.
-- recommendation_open: lo studente apre un consiglio (nessuna durata).
-- Statistiche della piattaforma solo aggregate e anonime (TODO legale in
-- services/orientation/package-info.java).
CREATE TABLE IF NOT EXISTS study_events (
  study_event_id       bigint      GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  student_id           bigint      NOT NULL REFERENCES students (student_id) ON DELETE CASCADE,
  kind                 text        NOT NULL,
  lesson_id            bigint      REFERENCES lessons (lesson_id),
  -- Le pagine si possono eliminare: l'evento resta legato alla lezione
  lesson_page_id       bigint      REFERENCES lesson_pages (lesson_page_id) ON DELETE SET NULL,
  resource_id          bigint      REFERENCES resources (resource_id),
  practice_exercise_id bigint      REFERENCES practice_exercises (practice_exercise_id),
  started_at           timestamptz NOT NULL,
  active_ms            bigint,
  created_at           timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_study_events_kind CHECK (kind IN ('lesson_read', 'resource_read', 'recommendation_open')),
  CONSTRAINT chk_study_events_target CHECK (
    (kind = 'lesson_read'         AND lesson_id IS NOT NULL AND resource_id IS NULL AND practice_exercise_id IS NULL)
    OR (kind = 'resource_read'    AND resource_id IS NOT NULL AND lesson_id IS NULL AND lesson_page_id IS NULL
                                  AND practice_exercise_id IS NULL)
    OR (kind = 'recommendation_open' AND practice_exercise_id IS NOT NULL AND lesson_id IS NULL
                                  AND lesson_page_id IS NULL AND resource_id IS NULL)),
  CONSTRAINT chk_study_events_active_ms CHECK (
    CASE WHEN kind = 'recommendation_open' THEN active_ms IS NULL
         ELSE active_ms IS NOT NULL AND active_ms > 0 END)
);
CREATE INDEX IF NOT EXISTS idx_study_events_student  ON study_events (student_id, started_at);
CREATE INDEX IF NOT EXISTS idx_study_events_lesson   ON study_events (lesson_id);
CREATE INDEX IF NOT EXISTS idx_study_events_page     ON study_events (lesson_page_id);
CREATE INDEX IF NOT EXISTS idx_study_events_resource ON study_events (resource_id);
CREATE INDEX IF NOT EXISTS idx_study_events_practice ON study_events (practice_exercise_id);

COMMIT;
