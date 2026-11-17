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

8.1. **Non fa parte del Blocco 5.** Si valuta una prima versione "beta" **dopo** averlo chiuso, a condizione
che non rompa il codice e non richieda di stravolgerlo.

8.2. Perché è fattibile senza stravolgimenti: per le regole già decise la rielaborazione **non cambia
superamento, punti o vite**, e la missione non la aspetta. È un'aggiunta accanto a un esercizio, non un passo
nuovo dello svolgimento. La tabella che la conserva esiste già.

8.3. **Vincolo per il Blocco 5:** lo svolgimento va scritto senza chiuderle la strada: ogni tentativo ha la
sua risposta e il suo esito, e una rielaborazione si potrà agganciare al tentativo senza modificarlo.

8.4. **Proposta per la versione beta (da decidere dopo il Blocco 5):**

- primo passo senza AI: lo studente scrive la rielaborazione scegliendo un contesto (per esempio uno sport) e
  l'Admin lascia un feedback dalla stessa pagina delle consegne;
- secondo passo con l'AI che propone correzione e feedback, sempre convalidati da una persona.

8.5. **TODO legale prima dell'AI:** i testi sono scritti da minorenni e verrebbero inviati a un servizio
esterno. Vanno indicati nell'informativa e serve il consenso previsto (Piano MVP, P.3).

## 9. Rappresentazione SQL

Nessuna migrazione prevista per il Blocco 5: tentativi, giudizi, checkpoint, record e accrediti hanno già le
loro tabelle. Scelte tecniche:

- Il record dei punti skill per esercizio di missione (§5) si legge dal registro degli accrediti
  (`skill_credit_entries`, origine `mission` e identificativo dell'esercizio della missione): non serve una
  tabella nuova.
- L'ordine delle opzioni mostrate allo studente si salva nella configurazione del tentativo, così ricaricare
  la pagina non le rimescola; un tentativo nuovo le mescola di nuovo (blueprint principale §6.2).
- "Annulla invio" riporta il tentativo da "in correzione" a "salvato": gli stati esistono già.
