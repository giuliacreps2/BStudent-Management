# BStudent — Motore degli errori e ripasso consigliato

**Versione:** v0.1 (proposta) · **Data:** 16 novembre 2026 · **Riferimento:** Blueprint delle regole v0.2 (7 novembre 2026), branch `feature/courses-development`.

Questo documento è una **proposta**: nessuna parte va considerata regola confermata finché non viene approvata. Si aggancia al blueprint senza modificarne le regole; dove una scelta tocca una regola esistente è segnalata in **Decisioni aperte** (§10).

Ispirazione: il modello *half-life regression* di Duolingo (Settles & Meeder, ACL 2016; codice di ricerca in `github.com/duolingo/halflife-regression`). Per ogni coppia studente–concetto si stima un'**emivita** della memoria: quanto tempo passa prima che la probabilità di ricordare scenda al 50%. Gli errori accorciano l'emivita, le risposte corrette la allungano, e il ripasso propone i concetti che lo studente sta per dimenticare.

---

## 1. Scopo e confini

**Cosa fa**

- Registra, per ogni risposta valutata in modo definitivo, **quali concetti** sono stati dimostrati o sbagliati.
- Mantiene per ogni studente una stima di quanto ricorda ogni concetto.
- Propone un **"Ripasso consigliato"**: al massimo 5 esercizi Palestra sui concetti più a rischio.
- Fornisce alla dashboard i dati per "Punti deboli" e per `recommendedActivity`.

**Cosa non fa (coerenza col blueprint)**

- Non modifica punti, record, accrediti skill/skin, voti, vite né superamento (§9.3, §10, §12).
- Non cambia l'ordine delle missioni: nella v0.1 il percorso è editoriale (§5.1). Il motore agisce **solo sulla Palestra** e sui suggerimenti.
- Non introduce obblighi: come il test di orientamento (§8), lo studente decide liberamente se seguire i consigli.
- Non usa AI: è un calcolo deterministico. L'AI resta limitata alla rielaborazione (§13).
- Il blueprint dice che i dati conservati "possono supportare analisi future" e che gli algoritmi sugli errori non diventano obbligatori (§10.5): questo documento è la proposta per renderli una funzionalità.

---

## 2. Perché serve un "concetto" e non basta la skill o l'argomento

| Livello esistente | Granularità | Limite per il ripasso |
|---|---|---|
| Skill (Grammatica, Lessico, Traduzione…) | Molto larga | "Grammatica debole" non dice cosa ripassare |
| Argomento (`topics`) | Media | Esiste solo sulle voci Palestra, non sulle domande delle missioni |
| **Concetto** (nuovo) | Fine: "ablativo assoluto", "3ª declinazione neutri", "aoristo sigmatico" | È l'unità che si ricorda o si dimentica |

Il concetto è l'equivalente del *lexeme* di Duolingo. Per il latino e il greco l'unità naturale è il fenomeno grammaticale o lessicale, non la parola.

---

## 3. Modello dei dati (proposta)

Convenzioni del blueprint §3: inglese, `snake_case`, plurali, catalogo separato dallo stato dello studente, percentuali 0–100, punti `NUMERIC(12,2)`.

### 3.1 Catalogo (editoriale, Admin)

**`concepts`**: catalogo dei concetti per materia.

| Colonna | Note |
|---|---|
| `concept_id` | PK |
| `subject_id` | FK `subjects`; il concetto appartiene a una materia |
| `topic_id` | FK `topics`, facoltativa; collega il concetto all'argomento Palestra |
| `code` | Univoco nella materia, es. `lat_abl_assoluto` |
| `name`, `description` | Testo didattico in italiano |
| `status`, `published_at`, `withdrawn_at` | Stessi tre stati editoriali del §5.4 |

**`question_concepts`**: quali concetti una domanda mette alla prova.

| Colonna | Note |
|---|---|
| `question_id` | FK `questions` (quindi legata alla versione, come la modalità di valutazione, §6.1) |
| `concept_id` | FK `concepts`, stessa materia dell'esercizio |
| `weight` | 1–100, somma 100 per domanda (stessa logica dei pesi skill) |

**`question_answer_concepts`**: il "misconcetto" di una risposta sbagliata (solo scelta multipla).

| Colonna | Note |
|---|---|
| `question_answer_id` | FK `question_answers`, solo opzioni con `is_correct = false` |
| `concept_id` | Il concetto che lo studente ha frainteso scegliendo questa opzione |

È il pezzo più utile: un distrattore ben costruito ("ha scelto il dativo invece dell'ablativo") dice esattamente *quale* errore è stato fatto, non solo che c'è un errore.

**`question_rubric_criteria.concept_id`**: colonna facoltativa. Ogni criterio della rubrica delle traduzioni di versione può puntare a un concetto, così la docente segna gli errori senza scegliere da un elenco libero.

### 3.2 Esito della correzione docente

**`assessment_concept_errors`**: concetti sbagliati segnalati dalla docente in una traduzione.

| Colonna | Note |
|---|---|
| `exercise_attempt_id` | FK `exercise_assessments` |
| `concept_id` | FK `concepts` |
| `category` | `structural` / `lexical` (riusa `RubricCategory`) |

È facoltativa per la docente: la sua regola resta "percentuale 0–100 e feedback scritto" (§6.3). Se non segna nulla, il motore usa solo la percentuale.

### 3.3 Fatti di apprendimento (stato dello studente)

**`learning_observations`**: registro append-only, un'osservazione per concetto per tentativo.

| Colonna | Note |
|---|---|
| `learning_observation_id` | PK |
| `student_id` | FK, coerente con il tentativo (FK composta, come nelle migrazioni v0.2) |
| `exercise_attempt_id` | FK `exercise_attempts` |
| `concept_id` | FK `concepts` |
| `outcome` | `NUMERIC(5,2)`, 0–100: quanto il concetto è stato dimostrato |
| `source` | `automatic` / `teacher` |
| `context` | `mission` / `practice` (riusa `ExerciseContext`) |
| `hint_used` | Se è stato usato l'aiuto del cappello nel tentativo |
| `observed_at` | Momento della valutazione definitiva (orario del server) |
| UNIQUE | `(exercise_attempt_id, concept_id)`: rende la scrittura idempotente (§15) |

**`student_concept_states`**: stato sintetico per studente–concetto, ricalcolabile dal registro.

| Colonna | Note |
|---|---|
| `student_id`, `concept_id` | PK composta |
| `half_life_hours` | `NUMERIC(10,2)` |
| `last_seen_at` | Ultima osservazione |
| `seen_count`, `correct_count`, `wrong_count` | Contatori (servono come input al modello della fase 2) |
| `updated_at` | |

Il blueprint chiede "un solo modello per lo stesso fatto" (§3). Il fatto è `learning_observations`; `student_concept_states` è una **proiezione** che si può buttare e ricostruire dal registro (come la vista dei totali skill). Va documentata così per non creare due fonti di verità.

---

## 4. Quando si scrive un'osservazione

Regola unica: **solo quando il giudizio diventa definitivo**.

| Situazione | Osservazione |
|---|---|
| Valutazione automatica (scelta multipla, completamento, traduzione di frase con alternative) | Sì, alla valutazione |
| Consegna alla docente (`pending_review`) | No, fino alla convalida (`validated_at`) |
| "Annulla invio" (§9.4) | No: non c'è un giudizio definitivo |
| Tentativo salvato, abbandonato o ritirato | No |
| Tempo scaduto con risposta vuota | **Aperto** (§10, D3) |
| Rielaborazione (§13) | No nella v0.1: il suo giudizio non modifica nulla e passa dall'AI |

Missione e Palestra contano entrambe: un errore resta un errore anche dentro una missione. Il motore legge il fatto senza cambiarne le conseguenze.

La scrittura avviene nella **stessa transazione** che rende definitivo il giudizio, oppure tramite evento deduplicato (§15: atomicità o consegna affidabile). L'indice UNIQUE impedisce i doppi conteggi se la richiesta viene ripetuta.

---

## 5. Da risposta a esito per concetto

| Tipo di domanda | Concetti della domanda (`question_concepts`) | Concetto del misconcetto |
|---|---|---|
| Scelta multipla, risposta corretta | `outcome = 100` | — |
| Scelta multipla, risposta sbagliata | `outcome = 0` | `outcome = 0` sul concetto dell'opzione scelta, se indicato |
| Completamento | 100 o 0 | — |
| Traduzione di frase automatica | 100 o 0 | — |
| Traduzione di frase con docente | percentuale della docente | concetti segnati dalla docente: `outcome = 0` |
| Traduzione di versione | percentuale della docente | concetti segnati dalla docente: `outcome = 0` |

Se lo stesso concetto compare sia tra quelli della domanda sia tra gli errori segnalati, prevale l'errore (`outcome = 0`).

Il peso in `question_concepts` non cambia l'esito: serve nella fase 2 come forza dell'evidenza (un concetto al 10% di una versione lunga conta meno di uno al 100% di un completamento).

---

## 6. Algoritmo

La probabilità di ricordare un concetto dopo `Δ` ore dall'ultima volta è:

```
p = 2^(−Δ / h)        h = emivita in ore
```

Con `Δ = h` la probabilità è 50%; con `Δ = 0` è 100%.

### 6.1 Fase 1: regola semplice (subito, senza dati storici)

All'arrivo di un'osservazione con esito `o` (0–100):

```
se o ≥ soglia_ripasso (default 60):  h ← min(h × fattore_successo, h_max)
altrimenti:                           h ← max(h × fattore_errore, h_min)
```

| Parametro | Default proposto | Dove |
|---|---|---|
| `h` iniziale | 24 ore | impostazioni dell'Academy |
| `fattore_successo` | 2,0 (1,5 se `hint_used`) | impostazioni |
| `fattore_errore` | 0,5 | impostazioni |
| `h_min` / `h_max` | 4 ore / 90 giorni | impostazioni |
| `soglia_ripasso` | 60, uguale alla soglia di superamento | impostazioni |

Come per le altre impostazioni (§5.7), cambiarle vale dal momento della modifica e non riscrive lo stato esistente.

È in pratica un sistema Leitner continuo: chi sbaglia rivede presto, chi risponde bene rivede sempre più tardi. Funziona dal primo giorno e si spiega facilmente alla docente.

### 6.2 Fase 2: half-life regression (quando ci sono dati)

Quando il registro avrà abbastanza osservazioni (indicativamente qualche migliaio, con studenti che ritornano sugli stessi concetti), l'emivita diventa:

```
h = 2^(θ · x)
x = [ √correct_count, √wrong_count, 1 (bias), indicatore del concetto ]
```

- `θ` si stima **offline** sul registro (script Python, partendo dal codice di ricerca Duolingo), confrontando la probabilità prevista con l'esito reale delle osservazioni successive.
- Il backend Java legge solo i pesi stimati (tabella `concept_model_weights` o file versionato) e calcola `h`. Nessun ML in produzione.
- L'indicatore del concetto impara che alcuni concetti sono "difficili" per tutti (es. il congiuntivo greco): partono con un'emivita più corta.

Il passaggio dalla fase 1 alla 2 non cambia le tabelle: cambia solo come si calcola `h` in `student_concept_states`, che si ricostruisce dal registro.

---

## 7. Selezione del "Ripasso consigliato"

1. Per lo studente, prendi i concetti con `p < 0,5`, ordinati per `p` crescente (prima quelli quasi dimenticati).
2. Per ogni concetto cerca le voci Palestra che lo mettono alla prova (tramite `question_concepts` sulla versione pubblicata), con questi filtri:
   - voce e esercizio pubblicati, nessun contenitore ritirato (§5.4, §7.1 v0.8);
   - livello di accesso compatibile con lo studente (§5.6);
   - nessuna bozza salvata né consegna in attesa sullo stesso esercizio (§7.2, §15);
   - preferisci esercizi non svolti di recente.
3. Elimina i duplicati (un esercizio può coprire più concetti a rischio: è un buon candidato).
4. Restituisci **al massimo 5 esercizi**, lo stesso limite del test di orientamento (§8).

Se lo studente non ha concetti sotto soglia, il ripasso è vuoto e la dashboard mostra un messaggio positivo, non esercizi a caso.

---

## 8. API (bozza)

Studente (proprietario, §4):

| Metodo | Endpoint | Restituisce |
|---|---|---|
| GET | `/me/review/recommendations` | Fino a 5 esercizi Palestra con il concetto che motiva ciascuno |
| GET | `/me/concepts` | Concetti visti con stato semplificato: "solido", "da ripassare", "a rischio" (mai la formula) |

Admin (`/admin/academy`, §5.7):

| Metodo | Endpoint | Uso |
|---|---|---|
| CRUD | `/admin/academy/concepts` | Catalogo concetti per materia |
| PUT | `/admin/academy/questions/{id}/concepts` | Tag della domanda (in bozza di versione) |
| PUT | `/admin/academy/answers/{id}/concept` | Misconcetto di un distrattore |

Docente: la schermata di correzione aggiunge una lista di concetti spuntabili, precompilata dai criteri della rubrica.

**Aggancio al frontend esistente** (branch `feature/backoffice-admin` dell'Academy):

- `components/exercises/TodayGoalSection.tsx` ("Qual è il tuo obiettivo oggi?") chiama `getRecommendedExercises(goal)` in `lib/exercise-recommendations.ts`, che oggi restituisce 3 esercizi da un elenco fisso. È il punto naturale del ripasso: con `goal = "mix"` restituisce i consigli del motore; con una skill scelta, i consigli filtrati su quella skill, con ripiego sugli esercizi Palestra della skill se il motore non ne ha.
- La dashboard ha già `recommendedActivity` (`RecommendedActivity` in `MidSection`): può mostrare il primo consiglio.
- Il frontend usa skill fisse (`SkillKey`: grammatica, lessico, analisi, traduzione) mentre il backend ha un catalogo skill dinamico: i consigli devono usare gli ID del catalogo, non le chiavi fisse.

Il controllo "missione pronta" (§5.5) può segnalare come **avviso** (mai blocco) le domande senza concetti: il motore ne ha bisogno, la pubblicazione no.

---

## 9. Invarianti e attenzioni

- Un'osservazione per `(tentativo, concetto)`; scrittura idempotente.
- Solo giudizi definitivi; "Annulla invio" e attese non lasciano traccia nel motore.
- Le osservazioni registrano la **versione svolta** tramite il tentativo: cambiare i tag di una versione successiva non riscrive lo storico (§6.1).
- Contenuti ritirati: le osservazioni restano (storico); il concetto o l'esercizio ritirato esce dai consigli.
- Lo studente legge soltanto: nessun setter su emivite o contatori (§15).
- Tempo dal server (§3): `observed_at` e `Δ` non arrivano mai dal client.
- **Minori (13–18 anni):** si tratta di dati di profilazione dell'apprendimento. Va verificato che l'informativa privacy e il consenso del genitore (`guardian_links`, `policy_acceptances`) coprano questo uso prima di attivarlo.

---

## 10. Decisioni aperte

| # | Domanda | Proposta |
|---|---|---|
| D1 | I tag dei concetti sono sulla **versione** della domanda (quindi modificabili solo con una nuova versione) o sull'**esercizio del banco**? | Sulla domanda, coerente con la modalità di valutazione; ammettere la modifica dei tag anche sulla versione pubblicata, perché non cambiano ciò che lo studente vede. È un'eccezione al §6.1 da approvare |
| D2 | Gli errori nelle **missioni** alimentano il ripasso Palestra? | Sì: l'errore è lo stesso fatto, cambia solo dove si ripassa |
| D3 | **Tempo scaduto** con risposta vuota | Non registrare: non dice nulla sul concetto. In alternativa `outcome = 0` con peso ridotto |
| D4 | Uso dell'**aiuto del cappello** | Successo con fattore ridotto (1,5 invece di 2,0) |
| D5 | Studente **free** con consigli su esercizi premium | Mostrarli col lucchetto, come le missioni premium (§5.6), oppure filtrarli |
| D6 | Concetti **senza esercizi Palestra** disponibili | Mostrarli in "Punti deboli" con link alla lezione nel Taccuino |
| D7 | La docente deve **segnare i concetti** o è facoltativo? | Facoltativo nella v0.1 |

---

## 11. Piano per blocchi

Il motore dipende dalle valutazioni, quindi parte **dopo il Blocco 5** (svolgimento delle missioni), ma il catalogo si può preparare prima.

| Passo | Contenuto | Dipende da |
|---|---|---|
| E1 | Migrazione: `concepts`, `question_concepts`, `question_answer_concepts`, `concept_id` sui criteri | — |
| E2 | Admin: catalogo concetti e tag nell'editor delle versioni; avviso in "missione pronta" | E1 |
| E3 | Migrazione: `learning_observations`, `student_concept_states`, `assessment_concept_errors` | E1 |
| E4 | Scrittura delle osservazioni alla valutazione definitiva (fase 1) | Blocco 5, E3 |
| E5 | `GET /me/review/recommendations` e `GET /me/concepts`; collegamento alla dashboard | E4, Blocco 4 (Palestra studente) |
| E6 | Correzione docente con concetti spuntabili | E3, correzione docente |
| E7 | Fase 2: script di stima offline e lettura dei pesi in Java | Dati reali sufficienti |

I test più importanti sono sulle regole del §4 (quando si scrive o no un'osservazione), sull'idempotenza e sui filtri del §7.
