# BStudent — Piano dell'MVP: cosa manca e con quale modello

Aggiornato il 16/11/2026. Comprende tutto l'MVP, non solo questo branch. Il dettaglio del Blocco 4 è in
`BStudent_TODO_Blocco4.md`, con le caselle da spuntare.

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

**Stime:** giorni pieni di lavoro, comprese prove e correzioni; sono esclusi la scrittura dei contenuti, la
revisione legale e il tempo delle tue decisioni. Sono stime, non promesse.

---

## 1. Blocco 4 — Letture dello studente (branch `feature/courses-development`)

**Deciso il 16/11/2026:** tutta la parte dei corsi resta al **Pro**, in questa chat. Fable lavora sulla gamification (§3).

Fatto: v0.9 applicata; Admin di materie, corsi, missioni, banco, lezioni e skill; interfacce dei service.

| # | Lavoro | Modello | Giorni |
|---|---|---|---:|
| 4.1 | Skill della materia, argomenti (`ISubjectSkillsService`, `ITopicsService`) | Pro | 1 |
| 4.2 | Voci della Palestra, test lato Admin, Risorse, utility `ContentBlocks`, entity mancanti | Pro | 2–3 |
| 4.3 | Form Admin esistenti con i campi nuovi (materie, corsi, missioni, banco, lezioni, skill) | Pro | 1–2 |
| 4.4 | Pagine Admin nuove: skill della materia, argomenti, Palestra, test, Risorse + **editor a blocchi** + caricamento delle immagini su Cloudinary tramite il backend | Pro | 3–4 |
| 4.5 | Letture dello studente: `StudentVisibility` e i service di `services.student`, test lato studente, Quaderno, registro di studio, struttura di Progressi | Pro | 3–5 |
| 4.6 | **Presentazione dei contenuti**: lettore della lezione (blocchi, pagine, "Ho finito di leggere", nota a lato), video YouTube e Vimeo con il play (fine del video = lezione nel Quaderno) | Pro | 2–3 |
| 4.7 | Frontend studente: 20 errori TypeScript, mock sostituiti con le chiamate vere, Risorse nell'area riservata, barra fissa, segnale di tempo attivo | Pro | 4–6 |
| 4.8 | Prova completa con uno studente `*@example.com` e correzioni | Pro | 1–2 |
| | **Totale Blocco 4** | | **17–26** |

Deciso il 16/11/2026: **YouTube non in elenco** per i video free, **Vimeo** per i premium, **Cloudinary** per le immagini (blueprint §5.1). Servono: account Cloudinary e chiavi in `env.properties` (per la 4.4), un piano Vimeo che limiti la riproduzione al dominio (per la 4.6).

## 2. Blocco 5 — Svolgimento, correzione, punti (branch nuovo)

| # | Lavoro | Modello | Giorni |
|---|---|---|---:|
| 5.1 | Contratti: svolgimento di missioni e Palestra, timer, vite, checkpoint, "Annulla invio", correzione docente, punti (blueprint §7.2, §9, §10, §12) | Pro con te | 1–2 |
| 5.2 | Backend dello svolgimento: tentativi, salvataggi, timer dal server, superamento, checkpoint, perdita di vite | **Fable** | 5–7 |
| 5.3 | Punti e progressi: contributi Palestra e missioni, record, media del corso, prima evoluzione | Pro | 2–3 |
| 5.4 | Frontend: **spazi per rispondere** (scelta multipla, completamento, traduzione di frase, versione, rielaborazione) e **missione in corso** (timer, vite, aiuto del cappello, esito) | **Fable** | 4–6 |
| 5.5 | Correzione docente: ruolo docente minimo, coda delle consegne, percentuale e feedback, rubriche | Pro | 3–4 |
| 5.6 | Dati veri in Palestra (stati, sfida giornaliera), Progressi e Quaderno ("Nuovo") | Pro | 1–2 |
| | **Totale Blocco 5** | | **16–24** |

## 3. Gamification e dashboard (branch dedicato)

| # | Lavoro | Modello | Giorni |
|---|---|---|---:|
| G.1 | Decisioni con te: curva dei livelli, soglie delle evoluzioni, media tra materie, streak, bonus, ricompense del corso, sblocco dei personaggi | **Fable** con te | 1–2 |
| G.2 | Migrazione (materia sui crediti skill, vista dei totali), XP dello studente, livelli skill, sblocco per livello, evoluzioni, accessori e cappello, vite e attività per ottenerle | **Fable** | 4–6 |
| G.3 | Pagina del personaggio, dashboard, home "cosa faccio ora", dati veri nella barra fissa | **Fable** | 2–3 |
| | **Totale gamification** | | **7–11** |

## 4. Blog (v0.10)

| # | Lavoro | Modello | Giorni |
|---|---|---|---:|
| B.1 | Mappa dei tipi del Blog e migrazione v0.10 (articoli, taglio, preferiti con FK, collegamenti dalle Risorse) | Pro | 1 |
| B.2 | Admin degli articoli (riusa l'editor a blocchi), lettura pubblica con taglio, preferiti nel Quaderno | Pro | 2–3 |
| | **Totale Blog** | | **3–4** |

## 5. Pagamenti, account, produzione

| # | Lavoro | Modello | Giorni |
|---|---|---|---:|
| P.1 | Stripe: checkout, abbonamenti, piano famiglia, webhook idempotenti, recesso, sconto beta (branch `feature/billing-development`: modello e accessi già fatti) | **Fable** | 5–7 |
| P.2 | Pagine di acquisto, richiesta al genitore, area abbonamento | Pro | 1–2 |
| P.3 | Account e GDPR: esporta e cancella i dati, consenso del genitore, utenti nell'Admin | Pro | 4–6 |
| P.4 | Testi e UX: copy delle pagine, stati vuoti, errori, accessibilità di base | Pro con te | 4–7 |
| P.5 | Produzione: hosting, variabili d'ambiente, backup automatici, log ed errori, dominio, email | Pro | 4–6 |
| P.6 | Test automatici dei service con più regole (visibilità, test, svolgimento, punti, Stripe) e revisione di sicurezza | Pro | 3–5 |
| P.7 | Stabilizzazione prima della beta: correzioni dalla prova con i primi studenti | Pro | 2–3 |
| | **Totale** | | **23–36** |

---

## Riepilogo

| Parte | Giorni | di cui con Fable |
|---|---:|---:|
| Blocco 4 | 17–26 | 0 |
| Blocco 5 | 16–24 | 14–20 |
| Gamification | 7–11 | 7–11 |
| Blog | 3–4 | 0 |
| Pagamenti, account, produzione | 23–36 | 8–12 |
| **Totale** | **66–101** | **29–43** |

Rispetto alla stima del 7/11 (50–75 giorni) il totale cresce per le aggiunte decise dopo:
- test di livello;
- Risorse;
- briefing e tutorial;
- registro di studio;
- presentazione dei contenuti;
- editor a blocchi;
- test automatici.

Fable non toglie lavoro, lo comprime. Le voci in grassetto sono quelle dove il tempo si accorcia di più, perché un
pacchetto intero esce da una sessione invece che da molti passaggi. **Ipotesi da verificare sul primo pacchetto
(gamification):** se Fable dimezza le sue voci, il totale scende a circa 52–80 giorni.

## Decisioni tue che bloccano il lavoro

| Prima di | Decisione |
|---|---|
| G.1 | curva dei livelli, soglie, streak e bonus |
| P.1 | prezzi e piani definitivi per la beta |
| P.3 | revisione legale: profilazione dei minori, informativa, consenso del genitore |
| dopo il Blocco 5 | motore degli errori: concetto = argomento? (`BStudent_Blueprint_Motore_Errori_v0.1.md`) |

## Fuori dall'MVP

Motore degli errori (dopo il Blocco 5, ricostruibile dallo storico), correzione con l'AI, classifica,
"Approfondisci" delle Risorse, "Mettiti alla prova", versione da stampare, evoluzioni proprie di ogni
personaggio, mondo virtuale con scuole e sponsor.
