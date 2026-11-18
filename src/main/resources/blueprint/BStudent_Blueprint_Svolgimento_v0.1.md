# BStudent — Blueprint dello svolgimento v0.1 (Blocco 5)

Iniziato il 17/11/2026 (branch `feature/courses-development`). Raccoglie le decisioni prese con Giulia sui punti
che il blueprint principale lasciava aperti: esercizi corretti dalla docente, "Annulla invio", che cosa vede lo
studente dopo un esercizio, chi corregge.

Le regole già decise restano nel blueprint principale (`BStudent_Blueprint_Regole_SQL_v0.2.md`): esercizi §6,
Palestra §7.2, missioni §9, punti §10, vite §12. Dove i due documenti parlano della stessa cosa, **vale
questo**, perché è più recente.

## 0. Come leggere questo documento

- **Deciso** = confermato da Giulia, con la data.
- **Da decidere** = domanda ancora aperta, con la proposta.
- **Da osservare** = scelta fatta, da ricontrollare con l'uso reale.
- **TODO dopo la beta** = rimandato di proposito.

## 1. Esercizio corretto dalla docente dentro una missione (deciso il 17/11/2026)

1.1. Un esercizio corretto dalla docente può stare **solo come ultimo esercizio** della missione. Dopo un
esercizio si procede solo se è superato (blueprint principale §9.1): a metà missione lo studente resterebbe
fermo per giorni.

1.2. **Nell'Admin l'errore si vede nel momento in cui si sbaglia**, non in un controllo a parte. Aggiungere o
spostare un esercizio corretto dalla docente in un punto diverso dall'ultimo viene rifiutato subito, con un
messaggio che dice cosa fare: "Un esercizio corretto dalla docente deve essere l'ultimo della missione:
spostalo in fondo". Lo stesso vale se si aggiunge un altro esercizio dopo di lui.

**Scelta tecnica (17/11/2026):** conta l'ultimo *esercizio*. Una lezione dopo di lui è ammessa: lo studente la
legge dopo l'esito della docente (il controllo "missione pronta" avvisa già quando una missione finisce con una
lezione). Gli esercizi ritirati non contano. Se una nuova versione dell'esercizio passa alla correzione della
docente, il problema compare anche fra quelli che bloccano la pubblicazione della missione.

1.3. **TODO di UX (Admin, frontend):** il riquadro "missione pronta" oggi non fa capire che cosa sta succedendo
(osservazione di Giulia del 17/11/2026). Va ridisegnato: ogni problema accanto al passo a cui si riferisce, con
l'azione da fare, al posto di un elenco in fondo alla pagina. Fino ad allora le regole nuove non si affidano a
quel riquadro.

1.4. **Dopo la consegna** la missione è "in attesa di correzione":

- il tempo è fermo e l'attesa non fa perdere vite;
- lo studente non può più usare "Abbandona missione" (blueprint principale §9.4);
- può fare la Palestra e le missioni di altri corsi.

1.5. **Esito della docente:**

| Esito | Che cosa succede |
|---|---|
| Almeno 60% | Missione superata: XP, punti skill, eventuale evoluzione |
| Sotto il 60% | Tentativo fallito e una vita persa, come per gli altri esercizi. Lo studente rifà solo quell'esercizio: gli altri restano superati |

## 2. "Annulla invio" (deciso il 17/11/2026)

2.1. Si può annullare **solo finché la docente non ha dato l'esito**. Dopo, il giudizio è definitivo (blueprint
principale §6.3).

2.2. Annullare **non fa perdere vite** e non fa fallire la missione (blueprint principale §9.4).

2.3. Lo studente **ritrova la sua risposta, modificabile, con il tempo che aveva al momento dell'invio**. Poi
può inviarla di nuovo. Non è un modo per guadagnare tempo: il tempo riparte da dove si era fermato.

Esempio: esercizio da 20 minuti, inviato dopo 14. Lo studente annulla: ritrova il testo e gli restano 6 minuti.

2.4. **Da osservare:** con il tempo o con molti studenti, le risposte conservate potrebbero pesare
sull'applicazione (osservazione di Giulia). Oggi ogni tentativo conserva una sola risposta, che è testo. Se il
volume cresce si potrà archiviare o alleggerire lo storico dei tentativi più vecchi.

## 3. Che cosa vede lo studente dopo un esercizio (deciso il 17/11/2026)

3.1. **Esercizio superato** (in missione o in Palestra): la **risposta giusta**, che c'è sempre. Spiegazione
generale e feedback della risposta sono facoltativi per l'autrice: si mostrano solo se esistono.

3.2. **Esercizio non superato in missione:** il feedback sulla risposta data, se esiste, e l'invito a
rileggere la teoria dell'argomento. **Non la soluzione:** lo studente deve rifare lo stesso esercizio e
basterebbe copiarla.

3.3. **Esercizio non superato in Palestra:** la soluzione si vede sempre. La missione è la prova, la Palestra
è l'allenamento.

3.4. È coerente con l'aiuto del cappello, che dice l'argomento e non la risposta (Blueprint Gamification §6.2).

## 4. Chi corregge (deciso il 17/11/2026)

4.1. Nella beta **corregge l'Admin**, da una pagina "Consegne da correggere": percentuale da 0 a 100 e feedback
scritto. Le rubriche restano una guida, senza punteggio per criterio (blueprint principale §6.1).

4.2. **TODO importante dopo la beta: ruolo docente separato.** Diventa fondamentale se l'applicazione cresce
(indicazione di Giulia): più docenti, ognuna con le sue consegne, senza i permessi dell'Admin sui contenuti.
Richiede gestione degli utenti, permessi e assegnazione delle consegne. Il codice della correzione va scritto
in modo che "chi può correggere" sia una regola in un punto solo, così aggiungere il ruolo non richiede di
riscriverlo.

4.3. **TODO da rivedere (indicazione di Giulia del 18/11/2026), per ora resta così:**

- **Giudizio definitivo:** oggi una consegna già corretta non si corregge di nuovo. Da rivedere se serve poter
  correggere un errore della docente (con quali effetti su punti, vite e superamento).
- **Avviso allo studente** quando arriva l'esito: dopo la beta. Oggi lo vede riaprendo la missione o la Palestra.

## 5. Punti skill quando si ripete un esercizio di missione (deciso il 17/11/2026)

5.1. Vale **la stessa regola della Palestra** (blueprint principale §10.1), applicata a ogni esercizio della
missione: rifare un esercizio dà punti skill solo per la parte in più rispetto al proprio risultato migliore,
con il fattore delle missioni (10%). Chiude il punto "da allineare" del blueprint principale §10.3.

5.2. I punti già ottenuti restano anche se la missione non viene superata (già deciso, §10.3).

5.3. Con più skill sull'esercizio, il confronto si fa skill per skill, secondo la quota di ciascuna.

Esempio: esercizio da 10 punti tutto su Grammatica. Primo risultato 70% → 0,70 punti skill. Rifatto con 90% →
+0,20. Rifatto con 80% → nulla. Esempio con due skill, 70% Traduzione e 30% Lessico, risultato 80%: 0,56 a
Traduzione e 0,24 a Lessico; rifatto con 100%: +0,14 e +0,06.

5.4. Gli **XP** restano come deciso: solo al primo superamento della missione (Blueprint Gamification §1).

## 6. Esercizio ritirato mentre lo studente lo sta svolgendo (deciso il 17/11/2026)

Un intervento dell'autrice sui contenuti non deve mai costare una vita o del lavoro allo studente.

- **In Palestra:** il tentativo in corso si chiude senza esito e la voce sparisce dall'elenco.
- **In missione:** nessuna vita persa. L'esercizio ritirato viene saltato e la missione prosegue dal passo
  successivo. Se era l'ultimo esercizio, la missione risulta superata con quelli rimasti.
- **Già in attesa della docente:** la consegna resta da correggere e vale normalmente.

## 7. "Annulla invio" in Palestra (deciso il 17/11/2026)

Esiste anche in Palestra, con le stesse regole delle missioni (§2): si annulla finché la docente non ha dato
l'esito e si ritrova la propria risposta, modificabile. In Palestra non ci sono né vite né tempo obbligatorio.

## 8. Rielaborazione (indicazione di Giulia del 17/11/2026)

La rielaborazione, in cui lo studente rispiega una regola con parole sue, è uno degli esercizi di punta della
piattaforma (blueprint principale §13).

8.1. **Deciso il 17/11/2026: entra nella beta, senza AI.** Per l'AI serve prima un parere legale (§8.5), quindi
per ora **corregge Giulia**. Si sviluppa come ultima parte del Blocco 5, dopo lo svolgimento e la pagina delle
consegne, a condizione che non rompa il codice e non richieda di stravolgerlo.

8.1-bis. **Da progettare insieme: come ridurre il carico della correzione.** Idee di partenza: giudizi rapidi
già pronti con una nota facoltativa al posto di percentuale e testo lungo (il giudizio non cambia punti né
superamento, §8.2); rielaborazione facoltativa per lo studente; un numero massimo per missione; una pagina
che le mostra una dopo l'altra.

8.2. Perché è fattibile senza stravolgimenti: per le regole già decise la rielaborazione **non cambia
superamento, punti o vite**, e la missione non la aspetta. È un'aggiunta accanto a un esercizio, non un passo
nuovo dello svolgimento. La tabella che la conserva esiste già.

8.3. **Vincolo per il Blocco 5:** lo svolgimento va scritto senza chiuderle la strada: ogni tentativo ha la
sua risposta e il suo esito, e una rielaborazione si potrà agganciare al tentativo senza modificarlo.

8.4. **Due passi:**

- nella beta, senza AI: lo studente scrive la rielaborazione scegliendo un contesto (per esempio uno sport) e
  Giulia lascia il feedback dalla pagina delle consegne;
- dopo il parere legale: l'AI propone correzione e feedback, sempre convalidati da una persona.

8.5. **TODO legale prima dell'AI:** i testi sono scritti da minorenni e verrebbero inviati a un servizio
esterno. Vanno indicati nell'informativa e serve il consenso previsto (Piano MVP, P.3).

## 9. Una missione in corso per materia (deciso il 17/11/2026)

9.1. Lo studente può avere **una missione in corso per ogni materia**: per esempio una di Latino e una di
Greco aperte insieme. Sostituisce "una sola missione in corso per studente" del blueprint principale §9.1.

9.2. Nella stessa materia resta una sola: chi prova ad aprirne un'altra legge "Hai già una missione di Latino
in corso: [titolo]". Una missione in attesa della docente non conta come "in corso" (§1.4).

9.3. Le vite restano uniche e condivise fra tutte le materie (blueprint principale §12.1).

## 10. Correzione automatica del testo (precisato il 17/11/2026)

Nel completamento e nella traduzione di frase con alternative si ignorano: maiuscole e minuscole, spazi
all'inizio e alla fine, **spazi doppi**. Tutto il resto deve essere preciso: punteggiatura, accenti, lettere e
gli spazi tra le parole. Esempio: "rosam  et" vale come "rosam et"; "ros am" no.

## 11. Rappresentazione SQL

Tentativi, giudizi, checkpoint, record e accrediti hanno già le loro tabelle. Serve una sola migrazione
(v0.11) per la missione in corso per materia (§9): `mission_runs` riceve la materia e l'indice "una missione
in corso" passa da studente a studente e materia. Scelte tecniche:

- Il record dei punti skill per esercizio di missione (§5) si legge dal registro degli accrediti
  (`skill_credit_entries`, origine `mission` e identificativo dell'esercizio della missione): non serve una
  tabella nuova.
- L'ordine delle opzioni mostrate allo studente si salva nella configurazione del tentativo, così ricaricare
  la pagina non le rimescola; un tentativo nuovo le mescola di nuovo (blueprint principale §6.2).
- "Annulla invio" riporta il tentativo da "in correzione" a "salvato": gli stati esistono già.

**Aggiornato il 17/11/2026 (svolgimento scritto):** serve una seconda migrazione, la **v0.12** (il Blog passa
alla v0.13). Aggiunge le **lezioni lette nel percorso** (`mission_run_lessons`): insieme ai checkpoint dicono a
che punto è lo studente, anche quando ci sono due lezioni di seguito o chiude la pagina durante una lezione. Il
Quaderno non basta: dice quali lezioni lo studente possiede, non dove si trova in una missione che sta ripetendo.

Altre scelte tecniche dello svolgimento:

- **Dove si trova lo studente:** il primo passo visibile non ancora fatto (lezione letta nel percorso o esercizio
  superato). Così un esercizio ritirato si salta da solo (§6) e una lezione aggiunta dall'autrice prima del
  punto raggiunto va letta. Calcolo in `utilities.MissionPath`, con i test.
- **Tempo:** lo segna il server. Il frontend salva la risposta ogni pochi secondi; se per più di **45 secondi**
  non arriva nulla, la missione va in pausa da sola, senza perdere vite: chi chiude il browser perde al massimo
  quei 45 secondi. Una consegna arrivata fino a **3 secondi** dopo lo scadere viene accettata (ritardo della rete).
- **Esito della docente** (§1.5): la pagina "Consegne da correggere" (`ISubmissionsService`, scritta il 18/11/2026)
  chiama `IMissionRunsService.onTeacherAssessed`, oppure per la Palestra `IPracticeProgressService.onAssessed`.
  Consegne dalla più vecchia, filtro per materia; commento obbligatorio; il giudizio è definitivo. "Chi può
  correggere" è in `ISubmissionsService.canAssess` (oggi l'Admin). La consegna resta bloccata mentre si salva
  l'esito, così lo studente non può annullare l'invio nello stesso istante.
  Superato: missione superata. Sotto la soglia: una vita persa e si rifà solo quell'esercizio. Se nel frattempo lo
  studente ha aperto un'altra missione della stessa materia, la prima aspetta che lui la riapra.
- **"Annulla invio"** riporta la missione in pausa: lo studente preme "Riprendi" e il tempo riparte da dove si era
  fermato. Non è possibile se intanto ha aperto un'altra missione della stessa materia (§9): il messaggio dice quale.
- **Punti skill della missione** per skill: somma dei migliori contributi degli esercizi (`mission_skill_contributions`).
- **Risultato della missione:** media degli esercizi superati nel percorso, pesata sui punti; è il record se è migliore.

**TODO (corso già completato):** il database vuole che un corso completato abbia tutte le missioni superate. Se
l'autrice pubblica una missione nuova in un corso già completato, oggi il completamento torna "non completato"
finché lo studente non supera anche quella. L'Evoluzione I resta e non si riassegna. Da decidere se mostrare
comunque "completato" (vedi anche `ICourseProgressService`).
