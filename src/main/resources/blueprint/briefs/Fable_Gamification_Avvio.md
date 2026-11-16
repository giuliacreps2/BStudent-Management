# Brief per Fable — Gamification di BStudent: decisioni, contratto, codice

Scritto il 16/11/2026 dalla sessione che lavora sui corsi (branch `feature/courses-development`).
Questa sessione si occupa **solo della gamification**. La parte dei corsi (catalogo, Palestra, Risorse,
Quaderno, letture dello studente) resta all'altra sessione: non modificarla.

---

## 1. Chi è la persona con cui lavori e come lavorare con lei

- È **Giulia**, sviluppatrice unica di BStudent, piattaforma per studiare latino e greco pensata per ragazzi
  13–18 anni. Scrivi sempre in **italiano**, frasi chiare e senza gergo.
- Approccio **MVP**: ciò che non serve alla beta (circa 20 studenti) diventa un **TODO** scritto, non codice.
- **Prima leggi il blueprint, poi fai domande.** Chiedi solo ciò che il blueprint lascia aperto, al massimo
  3–4 domande per volta, numerate, ognuna con la tua **proposta** e il motivo in una riga.
- Le **scelte tecniche** (nomi di colonne, CHECK, nullable, ordine dei passi) le prendi tu con il default più
  semplice e le scrivi nei commenti. A lei porti solo **decisioni di prodotto**: cosa vede lo studente, regole
  di gioco, cosa c'è nell'MVP.
- Non riaprire decisioni già prese (elenco nel §4).
- Riassunti brevi. Quando una cosa è fatta e verificata, dillo e basta.

## 2. Regole operative (obbligatorie)

- **Non fare commit**: li fa lei. Alla fine riassumi le modifiche e proponi il messaggio di commit (in inglese,
  come quelli esistenti).
- **Consenso prima di ogni migrazione o azione sul DB.** Prima di applicare una migrazione fai un backup con
  `pg_dump -Fc` nella cartella `db-backups/` (è nel `.gitignore`) e spiegale come l'hai fatto.
  `psql.exe` e `pg_dump.exe` stanno in `C:\Program Files\PostgreSQL\18\bin` (non sono nel PATH). Le
  credenziali stanno in `env.properties` (`DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`): passale come
  variabili `PG*` e **non stamparle mai**.
- **Segreti solo in `env.properties`**, mai nel codice né nei messaggi.
- **Nei test niente email reali**: solo indirizzi `*@example.com`.
- **Non cancellare file** senza la sua conferma.
- **Fine riga**: alcuni file sono CRLF (soprattutto nel frontend). Mantieni quello del file e verifica con
  `git ls-files --eol`. I file nuovi del backend vanno in LF.
- **Interfacce Admin** senza termini tecnici (niente "code", "pesi", "draft") e con un riscontro visibile a ogni
  salvataggio. Messaggi di errore del backend in italiano, che dicono cosa fare.
- Python non è installato: per gli script usa Node o bash.

## 3. Dove lavorare: un worktree separato

L'altra sessione continua a lavorare nella cartella principale. **Non cambiare branch lì**: lavora in una
cartella tua, con `git worktree`.

```bash
cd C:\Users\giuli\Documents\BStudent\BStudent-Management
git worktree add ../BStudent-Management-gamification -b feature/gamification-development
```

- Il branch parte dall'ultimo commit di `feature/courses-development`, con la **v0.9** già applicata al DB
  (skill della materia, argomenti, registro di studio).
- `env.properties` non è nel repository: chiedi a Giulia di copiarlo nella nuova cartella.
- Il **DB locale è condiviso** con l'altra sessione. Le tue migrazioni devono solo **aggiungere** (colonne
  nullable, tabelle, viste), mai togliere o rinominare. Così l'altro branch continua a partire con
  `ddl-auto=validate`.
- **Numero della migrazione:** usa il primo numero libero in `src/main/resources/db/` al momento della
  scrittura (oggi è il v0.10) e scrivi nel riepilogo quale hai usato. Il Blog prenderà il numero successivo.
- Frontend: `C:\Users\giuli\Documents\BStudent\BStudent-Academy\bstudent-academy` (Next.js 16, branch
  `feature/backoffice-admin`). Anche lì crea un worktree o un branch tuo solo quando arrivi alla Fase E, e
  chiedi prima a Giulia.

## 4. Già deciso: non riaprire

Fonti: blueprint `src/main/resources/blueprint/BStudent_Blueprint_Regole_SQL_v0.2.md` (§10.3, §10.4,
§10.4-bis, §10.4-ter, §11, §11.4, §12, §5.9, §7.3) e `services/gamification/package-info.java`.

- **XP = punti skin** del personaggio. Si accreditano solo al superamento della missione, senza riaccredito
  nelle ripetizioni (§10.3).
- **Punti skill:** fanno salire il livello della skill, che sblocca esercizi più difficili e contribuisce
  all'evoluzione. Nelle missioni il fattore è 10%, nella Palestra si accredita la differenza sul record (§10.1).
- **Skill per materia:** catalogo unico, ogni materia sceglie le sue (`subject_skills`); punti e livelli si
  contano per materia ("Grammatica · Latino" ≠ "Grammatica · Greco"). Una skill nuova entra nella media solo
  quando è pubblicata e ha esercizi (§10.4-ter).
- **Personaggio (§11.4):**
  - nell'interfaccia "Personaggio", "Evoluzioni" e "Accessori", mai "skin";
  - l'aspetto (capelli, abiti, colori) è fuori dall'MVP;
  - gli accessori appartengono allo studente, non al personaggio;
  - una soglia di un accessorio si raggiunge in **una materia qualsiasi**;
  - lo studente sceglie alla registrazione uno dei personaggi iniziali e può cambiarlo liberamente; gli altri
    restano "Prossimamente";
  - XP **dello studente**, con un contatore unico: tutti i personaggi mostrano lo stesso stadio. Le evoluzioni
    proprie di ogni personaggio vengono dopo la beta.
- **Prima evoluzione (v0.1):** al completamento del primo corso; la base delle vite passa da 5 a 7 (§10.4).
- **Cappello da turista:** si riscatta a 50 punti di Grammatica, a mano ("Riscatta"), e dà 5 aiuti con
  ricarica di 12 ore (§11.1, §11.3).
- **Vite e aiuti non si comprano mai.** ⚡ = vite, 📜 = cariche del cappello (§5.9).
- **Sfida giornaliera** "supera 3 oggi": almeno 60%, solo voci della Palestra, ora italiana, oggi **senza
  premio** (§7.3).
- **Classifica:** fuori dall'MVP.
- **Pagine (§10.5):** barra fissa (vite, XP e stadio, sfida del giorno, materia attiva), home "cosa faccio
  ora", Progressi "il mio percorso".
- **Roadmap lontana** (non progettarla ora, ma non chiuderle la strada): mondo virtuale tipo The Sims con
  scuole in gara, sponsor e premi.

## 5. Fase A — Decisioni con Giulia (prima di qualsiasi codice)

Porta queste domande a gruppi di 3–4, ognuna con la tua proposta. Dove il blueprint ha già una
**Proposta**, parti da quella.

1. **Curva dei livelli delle skill.** Proposta §10.4-bis: ogni livello chiede circa 1,5 volte i punti del
   precedente, curva unica per tutte le skill. Servono numeri: punti del livello 1, numero di livelli.
2. **Difficoltà delle voci della Palestra** e sblocco fino a "livello della skill + 1" (§10.4-bis): confermare.
   Che cosa vede lo studente su una voce bloccata.
3. **Evoluzioni:** quanti stadi, soglia di XP e livello medio minimo per ciascuno. **Aperto:** la media dei
   livelli usa tutte le materie dello studente o solo la migliore?
4. **Sblocco degli altri personaggi** ("Prossimamente"): come e quando.
5. **Accessori dell'MVP** oltre al cappello: quali, a quali soglie, con quale beneficio.
6. **Bonus del personaggio** del mockup ("+5% Grammatica"): esistono o si tolgono?
7. **Premio della sfida giornaliera:** resta senza premio o ne riceve uno (che non sia comprabile)?
8. **Streak** (giorni consecutivi, nel mockup della dashboard e della Palestra): definirla o toglierla. Utenti
   minorenni: niente pressione o ansia da "perdere la serie" (per esempio un giorno di pausa che non la azzera).
9. **Ricompense del corso** (badge, sfondo, titolo, "misteriosa" in `types/myCourses.ts`) e **oggetto
   ricompensa della missione** ("Elmo di Bronzo" in `types/missions.ts`): quali esistono davvero.
10. **XP per il profilo completato** (`ProfileCompletionData.xpReward` in `types/dashboard.ts`): sì o no.
11. **Personaggi della landing pubblica** ("Sblocca con 500 XP", `types/courseLanding.ts`): allinearli alle
    regole decise.
12. **Vite, punti non decisi del §12.2:** livelli di difficoltà dell'attività per ottenere vite, cosa succede
    quando finiscono le domande preparate.
13. **Regole delle skill nelle missioni** (TODO §10.3): se le skill devono bloccare la pubblicazione, la skill
    predefinita della materia come ripiego, il significato dei pesi.

Ogni risposta va scritta subito (Fase B). Se Giulia rimanda un punto, scrivilo come TODO con la tua proposta.

## 6. Fase B — Scrivere le regole

- Crea **`src/main/resources/blueprint/BStudent_Blueprint_Gamification_v0.1.md`** con tutte le regole decise,
  numerate, con esempi numerici, e una sezione finale "Rappresentazione SQL prevista".
- Nel blueprint principale aggiungi **solo** rimandi brevi nelle sezioni §10–§12 ("vedi Blueprint
  Gamification §x"). Così si evitano conflitti con l'altro branch, che modifica lo stesso file.
- Aggiorna i TODO in `services/gamification/package-info.java`.

## 7. Fase C — Contratto

Come nell'altra sessione: per ogni service scrivi l'interfaccia con i metodi come **TODO in commento**, con le
regole del blueprint e i DTO da creare. Esempi da imitare: `services/practice/interfaces/IPracticeCatalogService.java`
e `services/student/package-info.java`. Mostralo a Giulia prima di implementare.

Service probabili (da confermare in base alle decisioni): XP e stadi dello studente, livelli delle skill per
materia, sblocco delle voci per livello, personaggi (scelta e cambio), accessori (riscatto, equipaggiamento),
aiuto del cappello, vite e attività per ottenerle, dati della barra fissa e della dashboard.

**Confine con il Blocco 5:** lo svolgimento di missioni ed esercizi (tentativi, superamento, accrediti) non
esiste ancora. Scrivi la gamification come **calcoli sui fatti già registrati** (`skill_credit_entries`,
`mission_records`, `practice_records`, `course_results`, `student_first_evolutions`): così il Blocco 5 dovrà
solo registrare i fatti. Gli eventi in arrivo dal Blocco 5 vanno descritti come contratto (cosa deve chiamare
e quando) e restano TODO.

## 8. Fase D — Migrazione e backend

- Migrazione solo additiva (§3): per esempio materia su `skill_credit_entries`, vista `student_skill_totals`
  (oggi descritta in `entities/progression/StudentSkillTotal.java` ma **non esiste nel DB**), soglie su
  `skin_evolutions` e `skill_levels`, difficoltà sulle voci della Palestra, motto del personaggio, campi per
  streak e premi se decisi.
- Mostra la migrazione a Giulia **prima** di applicarla. Backup, poi la applichi, la riesegui una seconda volta
  per verificare che si possa ripetere, controlli i dati e avvii l'app con `validate`.
- Implementa i service del contratto. Codice di riferimento per lo stile: `services/catalog/MissionsService.java`,
  `services/skills/SubjectSkillsService.java`, `utilities/Reorders.java`. Record per i DTO, eccezioni
  `BadRequestException` / `ConflictException` / `NotFoundException`, commenti brevi in italiano.
- **Test automatici** per i calcoli: curva dei livelli, soglie delle evoluzioni, media tra materie, streak, vite
  e ricarica, riscatto degli accessori. Sono regole con numeri: i test le proteggono.
- Admin: personaggi ed evoluzioni (immagini Cloudinary), accessori e soglie, impostazioni di gioco
  (`gamification_settings`).

## 9. Fase E — Frontend (dopo l'ok di Giulia)

Tipi da rispettare (sono il contratto con il frontend, con i TODO concordati scritti dentro):
`types/character.ts`, `types/dashboard.ts`, `types/skills.ts`, parti di `types/missions.ts`,
`types/exercises.ts`, `types/myCourses.ts`, `types/courseLanding.ts`.

Pagina del personaggio, dashboard e home, barra fissa, pagine Admin della gamification.

## 10. Fuori da questa sessione

Catalogo, Palestra e Risorse lato Admin, letture dello studente, Quaderno, Blog, svolgimento (Blocco 5),
Stripe. Se una decisione della gamification richiede un cambiamento lì, **scrivilo come TODO** e avvisa Giulia:
lo farà l'altra sessione.

## 11. Alla fine di ogni fase

Breve riepilogo in italiano: cosa è deciso o fatto, file toccati, cosa resta aperto, messaggio di commit
proposto. Aggiorna le voci G.1–G.3 in `src/main/resources/blueprint/BStudent_Piano_MVP.md`.
