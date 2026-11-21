# BStudent — Piano dell'MVP: cosa manca e con quale modello

Aggiornato il 21/11/2026 (prima versione: 16/11/2026). Comprende tutto l'MVP, non solo questo branch. Il dettaglio
del Blocco 4 è in `BStudent_TODO_Blocco4.md`, con le caselle da spuntare.

**Stato al 18/11/2026:** in due giorni di calendario (16–18/11) è stato fatto il backend di circa 18–26 giorni
del piano: Admin di Palestra, test e Risorse, letture dello studente, gamification, svolgimento di Palestra e
missioni, punti e progressi, consegne da correggere. **Restano circa 25–40 giorni pieni** (prima: 66–101). Il
dettaglio è nel riepilogo in fondo.

**Stato al 21/11/2026 (frontend, branch `feature/mission-play`):** collegate al backend le Risorse (spostate
nell'area riservata, dentro Esercizi), la Palestra (skill, voci per skill e per argomento, obiettivo di oggi,
esercizio con bozza, consegna e "Annulla invio"), la **missione in corso** (lezione a pagine, video, esercizio con
tempo, vite, aiuto del cappello, esito, pausa, abbandono, "Ripassa"), i corsi (test di livello, "Continua la
missione"), la pagina del corso, la mappa, la pagina della missione (briefing, tutorial, "Inizia") e le
**Consegne da correggere** nell'Admin. Inseriti nel DB di sviluppo dati d'esempio riconoscibili ("esempio"),
con backup prima. Premi d'esempio (bonus, badge, "Elmo di Bronzo") lasciati com'erano: decisione in sospeso
(Gamification §6.3–6.4), raccolti in `constants/rewardPlaceholders.ts`. Giulia prova UI e UX.

## Come si dividono i modelli

| | **Fable** | **Pro** (il modello di questa chat) |
|---|---|---|
| Quando | pacchetti grandi, già specificati, con molte regole che si intrecciano | decisioni con te, contratti, lavoro meccanico o a piccoli passi |
| Perché | scrive in una sessione un pezzo intero e coerente: è qui che si accorcia il tempo | costa meno e va bene quando la regola è già chiara |
| Esempi | motore dello svolgimento, lettori degli esercizi, editor a blocchi, Stripe, test | interfacce, migrazioni, CRUD su un modello già fissato, form, mock da sostituire, testi |

**Regola di lavoro:** prima il **Pro** prepara con te il contratto (interfaccia con i metodi come TODO, regole
del blueprint, DTO). Poi **Fable** lo implementa in una sessione. Infine il **Pro** controlla, compila, prova e
ti riassume le modifiche. Ogni pacchetto per Fable parte da un **brief**: interfaccia + sezioni del blueprint +
voci di questo piano. Il Pro lo prepara prima di passare a Fable.

**Dal 17/11/2026** Fable lavora anche sui corsi e sul Blocco 5, nel branch `feature/courses-development`
(gamification già unita).

**Stime:** giorni pieni di lavoro, comprese prove e correzioni; sono esclusi la scrittura dei contenuti, la
revisione legale e il tempo delle tue decisioni. Sono stime, non promesse.

---

## 1. Blocco 4 — Letture dello studente (branch `feature/courses-development`)

Fatto prima del piano: v0.9 applicata; Admin di materie, corsi, missioni, banco, lezioni e skill; interfacce dei service.

| # | Lavoro | Modello | Giorni | Stato |
|---|---|---|---:|---|
| 4.1 | Skill della materia, argomenti (`ISubjectSkillsService`, `ITopicsService`) | Pro | 1 | **fatto** (16/11) |
| 4.2 | Voci della Palestra, test lato Admin, Risorse, utility `ContentBlocks`, entity mancanti | Pro | 2–3 | **fatto** (16/11) |
| 4.3 | Form Admin esistenti con i campi nuovi (materie, corsi, missioni, banco, lezioni, skill) | Pro | 1–2 | da fare (frontend) |
| 4.4 | Pagine Admin nuove: skill della materia, argomenti, Palestra, test, Risorse + **editor a blocchi** + caricamento delle immagini su Cloudinary tramite il backend | Pro | 3–4 | da fare (frontend; Cloudinary anche backend) |
| 4.5 | Letture dello studente: `StudentVisibility` e i service di `services.student`, test lato studente, Quaderno, registro di studio, struttura di Progressi | Pro | 3–5 | **fatto** (backend: letture 17/11; Quaderno, registro di studio, Progressi e correzione visibile allo studente 21/11). Restano le pagine nel frontend |
| 4.6 | **Presentazione dei contenuti**: lettore della lezione (blocchi, pagine, "Ho finito di leggere", nota a lato), video YouTube e Vimeo con il play (fine del video = lezione nel Quaderno) | Pro | 2–3 | backend di "Ho finito di leggere" fatto (18/11); resta il frontend |
| 4.7 | Frontend studente: 20 errori TypeScript, mock sostituiti con le chiamate vere, Risorse nell'area riservata, barra fissa, segnale di tempo attivo | Pro | 4–6 | **in gran parte fatto** (21/11): Risorse, Palestra, corsi, mappa, missione. Restano dashboard, barra fissa, personaggio, Quaderno, Progressi, segnale di tempo attivo e le pagine ancora vuote (`lessons/[id]`, `progress`, `rewards`, `settings`, `register`) |
| 4.8 | Prova completa con uno studente `*@example.com` e correzioni | Pro | 1–2 | da fare |
| | **Totale Blocco 4** | | **17–26** | |

Deciso il 16/11/2026: **YouTube non in elenco** per i video free, **Vimeo** per i premium, **Cloudinary** per le immagini (blueprint §5.1). Servono: account Cloudinary e chiavi in `env.properties` (per la 4.4), un piano Vimeo che limiti la riproduzione al dominio (per la 4.6).

## 2. Blocco 5 — Svolgimento, correzione, punti (branch `feature/courses-development`)

Le decisioni sono in `BStudent_Blueprint_Svolgimento_v0.1.md`. Migrazioni v0.11 e v0.12 applicate.

| # | Lavoro | Modello | Giorni | Stato |
|---|---|---|---:|---|
| 5.1 | Contratti: svolgimento di missioni e Palestra, timer, vite, checkpoint, "Annulla invio", correzione docente, punti (blueprint §7.2, §9, §10, §12) | Pro con te | 1–2 | **fatto** (17/11) |
| 5.2 | Backend dello svolgimento: tentativi, salvataggi, timer dal server, superamento, checkpoint, perdita di vite | **Fable** | 5–7 | **fatto** (Palestra 17/11, missioni 18/11) |
| 5.3 | Punti e progressi: contributi Palestra e missioni, record, media del corso, prima evoluzione | Pro | 2–3 | **fatto** (17–18/11) |
| 5.4 | Frontend: **spazi per rispondere** (scelta multipla, completamento, traduzione di frase, versione, rielaborazione) e **missione in corso** (timer, vite, aiuto del cappello, esito) | **Fable** | 4–6 | **fatto** (21/11), da provare nel browser; resta la rielaborazione (5.7) |
| 5.5 | Correzione docente: coda delle consegne, percentuale e feedback, rubriche come guida. Nella beta corregge l'Admin; il ruolo docente separato è dopo la beta | Pro | 3–4 | **fatto** (backend 18/11, pagina Admin 21/11; commento e percentuale per lo studente nel backend 21/11). Resta da mostrarli nel frontend |
| 5.6 | Dati veri in Palestra (stati, sfida giornaliera), Progressi e Quaderno ("Nuovo") | Pro | 1–2 | Palestra fatta (21/11); Progressi e Quaderno con la 4.5 |
| 5.7 | **Rielaborazione beta senza AI** (aggiunta il 17/11/2026, Svolgimento §8): corregge Giulia; prima va progettato con lei come ridurre il carico | Pro con te | 2–3 | da fare |
| | **Totale Blocco 5** | | **18–27** | |

## 3. Gamification e dashboard

| # | Lavoro | Modello | Giorni | Stato |
|---|---|---|---:|---|
| G.1 | Decisioni con te: curva dei livelli, soglie delle evoluzioni, media tra materie, streak, bonus, ricompense del corso, sblocco dei personaggi | **Fable** con te | 1–2 | **fatto** (16/11): `BStudent_Blueprint_Gamification_v0.1.md` |
| G.2 | Migrazione v0.10, XP, livelli skill, sblocco per livello, evoluzioni, accessori e cappello, vite, sfida giornaliera, tris, Serie, personaggi, dati per barra fissa e dashboard. Gli eventi dello svolgimento ora sono collegati (18/11) | **Fable** | 4–6 | **backend fatto** (16/11); restano le pagine dell'Admin e dello studente |
| G.3 | Pagina del personaggio, dashboard, home "cosa faccio ora", dati veri nella barra fissa | **Fable** | 2–3 | da fare (frontend). Servono anche personaggi e accessori nel DB (oggi nessuno) e le loro pagine Admin |
| | **Totale gamification** | | **7–11** | |

## 4. Blog (migrazione v0.13)

| # | Lavoro | Modello | Giorni | Stato |
|---|---|---|---:|---|
| B.1 | Mappa dei tipi del Blog e migrazione v0.13 (articoli, taglio, preferiti con FK, collegamenti dalle Risorse) | Pro | 1 | da fare |
| B.2 | Admin degli articoli (riusa l'editor a blocchi), lettura pubblica con taglio, preferiti nel Quaderno | Pro | 2–3 | da fare |
| | **Totale Blog** | | **3–4** | |

## 5. Pagamenti, account, produzione

| # | Lavoro | Modello | Giorni |
|---|---|---|---:|
| P.1 | Stripe: checkout, abbonamenti, piano famiglia, webhook idempotenti, recesso, sconto beta (branch `feature/billing-development`: modello e accessi già fatti) | **Fable** | 5–7 |
| P.2 | Pagine di acquisto, richiesta al genitore, area abbonamento. **Da rivedere (16/11/2026, stima compresa):** il percorso dell'adulto non è sviluppato abbastanza; va riprogettato con "Chiedilo a un genitore" e il riepilogo dei risultati (Blueprint Gamification §8-bis) | Pro | 1–2 |
| P.3 | Account e GDPR: esporta e cancella i dati, consenso del genitore, utenti nell'Admin. **Aggiunto il 16/11/2026 (stima da rivedere):** passo "La tua scuola" dopo la registrazione, con classe obbligatoria, scuola facoltativa e import delle scuole d'Italia (Blueprint Gamification §6.6) | Pro | 4–6 |
| P.4 | Testi e UX: copy delle pagine, stati vuoti, errori, accessibilità di base | Pro con te | 4–7 |
| P.5 | Produzione: hosting, variabili d'ambiente, backup automatici, log ed errori, dominio, email | Pro | 4–6 |
| P.6 | Test automatici dei service con più regole (visibilità, test, svolgimento, punti, Stripe) e revisione di sicurezza. **In parte già fatti:** 187 test al 18/11 | Pro | 3–5 |
| P.7 | Stabilizzazione prima della beta: correzioni dalla prova con i primi studenti | Pro | 2–3 |
| | **Totale** | | **23–36** |

---

## Riepilogo

### Stima del 16/11/2026

| Parte | Giorni | di cui con Fable |
|---|---:|---:|
| Blocco 4 | 17–26 | 0 |
| Blocco 5 | 16–24 | 14–20 |
| Gamification | 7–11 | 7–11 |
| Blog | 3–4 | 0 |
| Pagamenti, account, produzione | 23–36 | 8–12 |
| **Totale** | **66–101** | **29–43** |

Rispetto alla stima del 7/11 (50–75 giorni) il totale cresceva per le aggiunte decise dopo: test di livello,
Risorse, briefing e tutorial, registro di studio, presentazione dei contenuti, editor a blocchi, test automatici.

### Stima del 18/11/2026: che cosa resta

L'ipotesi del 16/11 era "se Fable dimezza le sue voci". Sul backend è andata meglio: in due giorni di calendario
è stato fatto il lavoro di circa **18–26 giorni** del piano (4.1, 4.2, gran parte della 4.5, G.1, backend della
G.2, 5.1, 5.2, 5.3, backend della 5.5).

| Che cosa resta | Giorni nel piano | Stima nuova | Perché |
|---|---:|---:|---|
| **Backend**: Quaderno, registro di studio e Progressi, rielaborazione beta, Blog, Cloudinary, Stripe, account e GDPR | circa 18–25 | **5–9** | regole già scritte nei blueprint: è il lavoro che ora va veloce |
| **Frontend**: form e pagine Admin, editor a blocchi, lettore delle lezioni, mock da sostituire, missione in corso, consegne da correggere, dashboard, acquisto, testi | circa 25–40 | **13–20** | velocità non ancora misurata; ogni pagina va guardata da te |
| **Produzione e prova con i primi studenti** (P.5, P.7) | 6–9 | **6–9** | dipende da servizi esterni e da persone vere, non dal codice |
| **Totale** | | **circa 25–40** | |

### Stima del 21/11/2026

Fatto il 21/11 nel frontend: 5.4, pagina delle consegne (5.5), Palestra (5.6) e gran parte della 4.7.
**Restano circa 20–30 giorni** di lavoro sul codice, più i tempi esterni:

- backend: rielaborazione beta, Cloudinary, Stripe, account e GDPR, Blog (Quaderno, registro di studio,
  Progressi e correzione visibile allo studente fatti il 21/11, sera);
- frontend: dashboard e barra fissa, personaggio, Quaderno, Progressi, lettore delle lezioni a blocchi, test di
  orientamento della Palestra, Admin (4.3, 4.4, personaggi e accessori), acquisto, testi, grafica immersiva;
- produzione e prova con i primi studenti.

**Aggiornamento del 21/11, notte:** fatte anche le pagine del **Quaderno** (linguette per materia, copertina a
scelta, segnalibri, lezione con nota salvata in automatico), di **"I miei progressi"** (i 5 gruppi, dettaglio del
tentativo con il commento della docente) e il **tempo di studio**, portato nell'MVP (backend
`GET /student/progress/study-time` e scheda in Progressi, tono neutro). Il segnale di lettura parte da lezioni
(missione e Quaderno) e Risorse; "Salva nel Quaderno" delle Risorse salva il segnalibro; la missione riaperta
mostra il commento della docente. La linguetta "Articoli" resta con il Blog (serve la migrazione v0.13).

**TODO nato il 21/11:** pagina Admin con il tempo realmente usato per ogni esercizio (tempo concesso meno tempo
rimasto alla consegna), per fissare tempi realistici. La regola del tempo scaduto resta quella del blueprint.

Due avvertenze:

- **I bug verranno fuori con il frontend.** Lo svolgimento è coperto dai test ma non è ancora stato provato
  nell'app vera: le correzioni sono comprese nella stima, ma ci saranno.
- **I tempi più lunghi ora sono fuori dal codice:** revisione legale (dati dei minori, consenso del genitore),
  prezzi definitivi, account Stripe, hosting, dominio ed email, contenuti. Conviene avviarli subito, in parallelo.

## Decisioni tue che bloccano il lavoro

| Prima di | Decisione |
|---|---|
| G.1 | ~~curva dei livelli, soglie, streak e bonus~~ decise il 16/11/2026 |
| 5.7 | come ridurre il carico della correzione delle rielaborazioni (Svolgimento §8.1-bis) |
| P.1 | prezzi e piani definitivi per la beta |
| P.3 | revisione legale: profilazione dei minori, informativa, consenso del genitore |
| dopo il Blocco 5 | motore degli errori: concetto = argomento? (`BStudent_Blueprint_Motore_Errori_v0.1.md`) |
| da rivedere | correzione già data: resta definitiva o si può correggere un errore? (Svolgimento §4.3) |
| quando serve | corso già completato e missione nuova pubblicata: mostrare comunque "completato"? (Svolgimento §11, TODO) |
| in sospeso (21/11) | premi: bonus del personaggio, badge, ricompensa misteriosa, "Elmo di Bronzo" (Gamification §6.3–6.4) |
| prima della grafica | domande aperte del 21/11: dove lo studente legge la correzione, consegna in correzione in Palestra, testi della sfida del giorno |

## Fuori dall'MVP

Motore degli errori (dopo il Blocco 5, ricostruibile dallo storico), correzione con l'AI, uso dell'AI tra i
vantaggi dei piani in abbonamento, classifica, ruolo docente separato (Svolgimento §4.2), avviso allo studente
quando arriva la correzione, "Approfondisci" delle Risorse, "Mettiti alla prova", versione da stampare, evoluzioni
proprie di ogni personaggio, mondo virtuale con scuole e sponsor.
