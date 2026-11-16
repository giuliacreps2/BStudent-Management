# Brief per Fable — pacchetto 4.2: Admin di Palestra, test e Risorse + formato a blocchi

Repository backend: `C:\Users\giuli\Documents\BStudent\BStudent-Management`, branch `feature/courses-development`.
Spring Boot 4, Java 25, JPA/Hibernate (`ddl-auto=validate`), PostgreSQL 18. Il DB è già alla **v0.9**
(`src/main/resources/db/bstudent_migration_v0.9.sql`): **non scrivere migrazioni e non toccare il DB**.

## Obiettivo

Implementare il lato **Admin** di tre parti, già specificate come contratto nelle interfacce:

1. **Voci della Palestra** — `services/practice/interfaces/IPracticeCatalogService.java`
2. **Test di orientamento e di livello, parte Admin** — `services/orientation/interfaces/IOrientationService.java`
   (solo il blocco "Admin"; la parte "Studente" resta commento per la Fase 3)
3. **Risorse** — `services/resources/interfaces/IResourcesService.java`
4. **Utility `ContentBlocks`** — validazione del formato a blocchi, usata da Risorse e pagine della lezione

Nelle interfacce i metodi sono scritti come commento TODO: trasformali in metodi veri, con la stessa firma
salvo motivi chiari (da segnalare nel riepilogo finale).

## Da leggere prima di scrivere codice

- Le tre interfacce sopra e `services/skills/interfaces/ISubjectSkillsService.java`.
- Blueprint `src/main/resources/blueprint/BStudent_Blueprint_Regole_SQL_v0.2.md`: §5.4 (stati editoriali),
  §5.6 (free/premium), §7.1, §7.3, §7.4 (Risorse e formato a blocchi), §8 (test), §5.1 (video e immagini).
- Migrazione v0.9, sezioni 4–7: nomi esatti di colonne, CHECK e vincoli.
- **Codice di riferimento per lo stile** (copiarne le convenzioni):
  - `services/catalog/MissionsService.java` e `CoursesService.java`: stati, publish/withdraw/delete, riordino;
  - `services/skills/SubjectSkillsService.java` e `services/practice/TopicsService.java`: scritti per ultimi;
  - `controllers/catalog/MissionController.java`, `controllers/practice/TopicController.java`;
  - `entities/exercises/MissionExerciseSkill.java`: chiave composta con `@EmbeddedId` e `@MapsId`;
  - `utilities/Reorders.java`, `utilities/Slugs.java`, `utilities/Pageables.java`.

## Convenzioni obbligatorie

- Pacchetti: `entities/<dominio>`, `repositories/<dominio>`, `payloads/<dominio>`, `services/<dominio>`
  (+ `interfaces`), `controllers/<dominio>`. Record per i DTO; `XxxResponseDTO.from(entity)`.
- Controller sotto `/admin/academy/**` (già protetto per ADMIN in `SecurityConfig`). Liste corte = `List`,
  liste lunghe = `PagedModel` come `SubjectController`.
- Eccezioni: `BadRequestException`, `ConflictException`, `NotFoundException`. **Messaggi in italiano, senza
  termini tecnici**, che dicono cosa fare (es. "L'argomento appartiene a un'altra materia"). L'Admin li mostra
  così come sono.
- Commenti in italiano, brevi, solo dove spiegano il perché, con lo stesso tono del codice esistente.
- Enum in minuscolo come nel DB (`ContentStatus.draft`). `@Enumerated(EnumType.STRING)`, `columnDefinition = "TEXT"`.
- Stati editoriali: `published_at` è la prima pubblicazione e resta; `withdrawn_at` presente se e solo se ritirato.
- Riordini: `Reorders.validateIds` + `Reorders.deferUniqueOrder(entityManager, "<nome vincolo>")`.
- Chiavi composte già valorizzate: `entityManager.persist`, non `save`.
- **Fine riga**: mantenere quello del file (alcuni file sono CRLF, ad es. `payloads/catalog/MissionDTO.java`).
  I file nuovi in LF. Non convertire file esistenti.
- **Non fare commit.** Lascia tutto nel working tree.

## 1. Voci della Palestra

- Entity `PracticeExercise` esiste: aggiungi `estimatedMinutes` (`Short`/`Integer`, colonna `estimated_minutes`).
- La skill deve essere della materia dell'argomento: `ISubjectSkillsService.assertSkillOfSubject`.
- L'esercizio del banco compare al massimo una volta in Palestra (UNIQUE): 409 con messaggio chiaro.
- `publish`: solo se l'esercizio del banco è `published` (quindi con versione e titolo per lo studente).
- `delete`: solo se mai pubblicata e senza tentativi (`exercise_attempts.practice_exercise_id`, verifica il nome
  reale della colonna) né consigli dei test (`orientation_recommendations`).
- Response con titolo per lo studente dell'esercizio, argomento, skill e materia (id e nome).
- Filtri dell'elenco: materia, argomento, skill, stato, tutti facoltativi. Query semplice (JPQL con parametri
  nullable), **niente Specification**.

## 2. Test, parte Admin

- `OrientationTest`: aggiungi `kind` (enum nuovo `OrientationTestKind`: `exercises`, `course_level`, colonna `kind`).
  **Togli `unique = true` e il commento "Un solo test di orientamento per materia"**: ora l'unicità è
  `(subject_id, kind)` (`uq_orientation_tests_subject_kind`). Valuta se `@OneToOne` va cambiata in `@ManyToOne`
  (sì: una materia ha fino a due test).
- `OrientationOption`: aggiungi `level` (`CourseLevel`, nullable).
- Regole tra tabelle, nel service:
  - test `course_level`: ogni opzione ha un livello e nessun esercizio consigliato;
  - test `exercises`: nessun livello; consigli = voci della Palestra **della stessa materia**, priorità > 0;
  - opzione già scelta in un risultato salvato non si elimina. La tabella `orientation_results` esiste ma
    l'entity `OrientationResult` si crea nella Fase 3: per ora controlla con una query nativa su
    `orientation_results.answers` (jsonb, `{"<questionId>": <optionId>}`).
- Riordino di domande e opzioni come le altre liste (vincoli unici `(test, sort_order)` e
  `(question, sort_order)`: verifica nel DB se sono DEFERRABLE; se non lo sono, segnalalo nel riepilogo
  invece di scrivere una migrazione).
- `checkReadiness(testId)`: almeno 3 domande, ognuna con almeno 2 opzioni, e le regole sopra. Restituisce
  la lista dei problemi (vuota = pronto). Nessuno stato pubblicato per i test nell'MVP: la lettura studente
  userà solo i test pronti.

## 3. Risorse

- Entity nuove: `Resource` (`resources`), `ResourceLink` (`resource_links`, solo entity e repository: **nessun
  service né controller**, la sezione "Approfondisci" non è nell'MVP). Enum `ResourceCategory`
  (`grammatica`, `verbi`, `traduzione`, `vocabolario`). Pacchetto `resources`.
- Indirizzo con `Slugs.resolve`, unico nella materia. Argomento facoltativo **della stessa materia**.
- "Più usata" ↔ icona: insieme o nessuno (`chk_resources_most_used`). Icona = chiave in formato slug.
- `publish`: titolo, descrizione e almeno una sezione con contenuto (tabella, approfondimenti o esempi).
- `delete`: solo se mai pubblicata.
- **Contenuto `content` (jsonb)**: deve seguire esattamente i tipi del frontend in
  `C:\Users\giuli\Documents\BStudent\BStudent-Academy\bstudent-academy\types\resourceDetail.ts`
  (`ResourceTableTool`, `ResourceInsight`, `ResourceExample`) e `types/article.ts` (`ArticleBlock`):
  ```json
  {
    "summaryExample": { "term": "lex, legis", "translation": "legge" },
    "tool": { "type": "table", "title": "...", "variants": [ { "id": "...", "label": "...",
              "columns": ["Caso", "Singolare", "Plurale"], "rows": [["...", "...", "..."]], "note": "..." } ] },
    "insights": [ { "id": "...", "title": "...", "defaultOpen": false, "blocks": [ /* ArticleBlock */ ] } ],
    "examples": [ { "id": "...", "term": "...", "grammar": "...", "sentence": "...", "translation": "..." } ]
  }
  ```
  Ogni sezione è facoltativa. Ogni riga della tabella ha tanti valori quante colonne.
  **Nota:** il mini esempio de "In breve" ha due parti (`term`, `translation`), mentre la colonna
  `summary_example` è un testo unico. Usa `content.summaryExample` e **non usare** la colonna
  `summary_example`: aggiungi un commento TODO nell'entity ("colonna da togliere con la v0.10").
  `summary_text` resta la colonna del testo de "In breve".
- Mappare jsonb: `@JdbcTypeCode(SqlTypes.JSON)` su un campo `String` o su una struttura di record. Scegli
  la soluzione più semplice e coerente; la validazione avviene comunque con `ContentBlocks`.

## 4. Utility `ContentBlocks`

`utilities/ContentBlocks.java`: valida e normalizza i blocchi e il contenuto delle Risorse, con errori
in italiano che indicano la posizione ("Blocco 3: il paragrafo è vuoto").

- Blocchi ammessi (`types/article.ts`): `heading` (text, number facoltativo), `paragraph` (text), `quote`
  (text, author facoltativo), `callout` (text, icon facoltativo), `image` (src = identificativo Cloudinary,
  alt obbligatorio, caption facoltativa), `list` (items non vuoti, ordered facoltativo). Ogni blocco ha un `id`
  stringa unico nell'elenco. Nessun altro campo e nessun altro tipo (il blocco "taglio" arriverà con il Blog).
- Nei testi solo grassetto `**...**` e corsivo `*...*`: niente HTML (rifiuta `<` e `>`). Limiti ragionevoli di
  lunghezza (es. 5000 caratteri per paragrafo, 200 blocchi) da dichiarare come costanti.
- Le pagine di testo della lezione **non vanno ancora convertite**: oggi `formatted_text` è testo libero e il
  frontend non è pronto. Prepara solo il metodo che valida un elenco di blocchi e lascia nel TODO di
  `LessonPageDTO` che va collegato quando si fa l'editor (pacchetto 4.4).

## Fuori da questo pacchetto

Letture dello studente, invio dei test e calcolo dei risultati, segnalibri, Quaderno, frontend, caricamento
su Cloudinary, migrazioni.

## Come verificare

1. `./mvnw -q -o compile` senza errori (e `test-compile`).
2. Avviare l'app (`./mvnw -q -o spring-boot:run`) e controllare che parta: con `validate` un errore di
   mapping blocca l'avvio. Poi fermarla.
3. Non servono chiamate HTTP: non c'è un utente Admin di prova a disposizione.

## Riepilogo finale (obbligatorio)

Alla fine scrivi, in italiano e senza gergo:
- file creati e modificati;
- metodi implementati per ogni interfaccia, con eventuali differenze dalla firma prevista e il perché;
- regole del blueprint che non hai potuto rispettare o che hai dovuto interpretare;
- problemi trovati nel DB o nel codice esistente (es. vincoli non DEFERRABLE).
Aggiorna anche le caselle della Fase 1 in `src/main/resources/blueprint/BStudent_TODO_Blocco4.md`.
