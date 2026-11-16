# BStudent — TODO del Blocco 4 (branch feature/courses-development)

Lista del codice da scrivere per completare le parti del blueprint decise fino al 14/11/2026.
Ordine di lavoro: dall'alto in basso. Ogni fase si chiude con l'app avviata e provata.

**Fuori da questo branch:** gamification (XP, livelli skill, personaggio, accessori, streak, dashboard; materia su
`skill_credit_entries` e vista `student_skill_totals`), Blog (mappa + migrazione v0.10), Blocco 5 (svolgimento di
missioni ed esercizi, correzione docente, punti).

---

## Fase 0 — Database

- [x] Scrivere `bstudent_migration_v0.9.sql`
- [x] Applicare la v0.9 al DB locale (con consenso; backup in `db-backups/`)
- [x] Avviare l'app: le entity devono coincidere con il DB (`ddl-auto=validate`)

## Fase 1 — Backend Admin (contenuti)

Ogni voce rimanda alla sua interfaccia: lì ci sono i metodi (come TODO) e le regole del blueprint da rispettare.

- [x] Materie: indirizzo, tagline, immagine, guida del briefing, Pubblica/Nascondi
- [x] Corsi: indirizzo, tagline, livello (obbligatorio per pubblicare), in evidenza, tema del mondo, citazione
- [x] Missioni: indirizzo, tagline, copertina, prova finale (una per corso), video e battute del briefing, "Cosa imparerai"
- [x] Esercizi del banco: titolo per lo studente (obbligatorio per pubblicare), argomento
- [x] Lezioni: argomento della stessa materia
- [x] Skill: icona e colore
- [ ] Skill della materia (`subject_skills`): elenco, aggiungi, togli, riordina; i pesi delle missioni e le voci della Palestra usano solo skill della materia → `ISubjectSkillsService`
- [ ] Argomenti (`topics`): crea, modifica (con immagine), elimina se non usati — oggi `TopicsService` è vuoto → `ITopicsService`
- [ ] Voci della Palestra (`practice_exercises`): crea, modifica (skill, argomento, punti massimi, tempo stimato, free/premium), pubblica, ritira — oggi `PracticeCatalogService` è vuoto → `IPracticeCatalogService`
- [ ] Test (orientamento e livello): test per materia e tipo, domande, opzioni (esercizi consigliati con priorità oppure livello), almeno 3 domande per pubblicare — oggi `OrientationService` è vuoto → `IOrientationService` (parte Admin)
- [ ] Risorse: crea, modifica, pubblica, ritira; categoria, argomento, free/premium, "più usata" con icona, "in evidenza"; contenuto JSON controllato → `IResourcesService`
- [ ] Formato a blocchi: validazione JSON comune a pagine della lezione e Risorse, al posto del testo libero (`LessonPageDTO`) → utility `ContentBlocks`, descritta in `IResourcesService`
- [ ] Entity ancora da mappare: `Student` (tutorial visto, colore del Quaderno), `OrientationTest.kind`, `OrientationOption.level`, `PracticeExercise.estimatedMinutes`; nuove `OrientationResult`, `Resource`, `ResourceLink`, `ResourceBookmark`, `StudyEvent`

## Fase 2 — Frontend Admin (bstudent-academy)

Senza termini tecnici e con un riscontro visibile su ogni salvataggio.

- [ ] Form di materie (con Pubblica/Nascondi), corsi, missioni, esercizi del banco, lezioni, skill: campi nuovi della Fase 1
- [ ] Pagine Admin nuove: skill della materia, argomenti, voci della Palestra, test, Risorse (con editor a blocchi)

## Fase 3 — Backend letture studente

Una sola regola di visibilità (pubblicato + contenitori pubblicati + materia pubblicata), endpoint protetti in
`SecurityConfig`, anteprima Admin `?preview=true`: regole comuni nel package-info di `services.student`.

- [ ] Materie e corsi (`myCourses`, `courseLanding`): "Presto disponibile", in evidenza, popolare = false, corso di riferimento e 5 tappe, badge calcolati (blueprint §5.8) → `IStudentCatalogService`
- [ ] Mappa del corso e pagina della missione: stato dall'ordine, lucchetto premium, durata, XP e punti skill massimi, stepper, briefing (§5.9) → `IStudentMissionsService`
- [ ] Tutorial della prima volta: segnare come visto → `IStudentMissionsService`
- [ ] Test: invio delle risposte, calcolo (regola prudente per il livello, ordine per l'orientamento), salvataggio dei risultati (§8) → `IOrientationService` (parte studente)
- [ ] Palestra: materia attiva, skill della materia, elenco per skill raggruppato per argomento, obiettivo di oggi (§7.3) — stati e sfida giornaliera si riempiono con il Blocco 5 → `IStudentPracticeService`
- [ ] Risorse: pagina principale, dettaglio, ricerca, "Vai agli esercizi", segnalibri (§7.4) → `IStudentResourcesService`, segnalibri in `INotebookService`
- [ ] Quaderno: elenco per materia + Articoli, lezione con nota (salvataggio automatico), colore della copertina, segnalibri, regole su lezioni ritirate e premium scaduto (§14) → `INotebookService`
- [ ] Registro di studio: ricevere i tratti di lettura della teoria e i consigli aperti (§10.5) → `IStudyEventsService`
- [ ] Progressi: struttura dei 5 gruppi (§10.5) — i dati arrivano con il Blocco 5 e la gamification → `IProgressQueryService`

## Fase 4 — Frontend studente

- [ ] Sistemare i 20 errori TypeScript esistenti (`npm run build` deve passare)
- [ ] Applicare i TODO scritti nei `types/*.ts` e sostituire i mock con le chiamate vere, un tipo alla volta: myCourses, courseLanding, missions, exercises, resources, notebook
- [ ] Spostare le Risorse nell'area riservata (la copia SEO resta in `app/SEO_criteria`)
- [ ] Barra fissa (vite, XP, sfida, materia attiva) e segnale di tempo attivo per lezioni e Risorse

## Fase 5 — Prova completa

- [ ] Percorso end-to-end con uno studente `*@example.com`: materia → corso → missione (lettura) → Palestra → Risorse → Quaderno
