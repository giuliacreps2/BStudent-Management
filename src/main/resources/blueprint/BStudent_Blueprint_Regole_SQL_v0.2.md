# BStudent — Blueprint delle regole del backend e dello schema SQL

**Versione:** v0.2 · **Aggiornamento:** 7 novembre 2026 (v0.1: 12 ottobre 2026) · **Backend scelto:** Java con Spring · **Database progettato:** PostgreSQL.

**Novità della v0.2:** decisioni prese durante lo sviluppo del catalogo Admin e applicate con le migrazioni v0.6, v0.7 e v0.8: stati editoriali, versione pubblicata degli esercizi, freemium, impostazioni dell’Academy, date editoriali e sequenza libera della missione (lezioni ed esercizi in un unico ordine). Le parti della v0.1 superate sono marcate **[Superato in v0.x]** e riassunte nel §16.

Questo documento raccoglie le regole definite per il modello dei dati e le operazioni. È il riferimento funzionale per scrivere entity, repository, service e API. Non è una dichiarazione che il SQL precedente implementi già ogni regola, né un documento di codice eseguito.

## 1. Come leggere il blueprint

Le decisioni esplicite più recenti prevalgono sui documenti precedenti. In particolare, le correzioni del 9 ottobre su ritiro dei contenuti, timer, annullamento dell’invio e accesso al recupero vite sono incluse qui, anche quando mancano nel SQL consolidato. Le decisioni della v0.2 (dal 30 ottobre al 7 novembre 2026) prevalgono sulla v0.1 e sono già rappresentate nelle migrazioni e nel codice Java.

Sono distinti tre livelli:

- **Regola confermata:** comportamento deciso in intervista o nella conversazione.
- **Rappresentazione SQL:** struttura o vincolo presente nelle bozze; non basta, da solo, a dimostrare l’approvazione di una regola di prodotto.
- **Proposta/aperto:** soluzione tecnica non confermata o comportamento non ancora definito; non va trasformato automaticamente in requisito.

Il lavoro prosegue sul codice Java per blocchi. Non occorre riaprire tutta l’intervista: eventuali discrepanze si risolvono nel blocco interessato. Non si eliminano tabelle soltanto perché numerose. Le strutture utili in futuro possono restare, senza richiedere subito metodi, endpoint o schermate.

## 2. Perimetro e responsabilità

| Area | Responsabilità |
|---|---|
| Identità | Utenti, studenti, ruoli, token, scuola, legami genitore e accettazioni; strutture ereditate dal SQL fornito |
| Catalogo | Materie, corsi, missioni con la loro sequenza di lezioni ed esercizi, contenuti disponibili, impostazioni dell’Academy |
| Esercizi | Identità dei contenuti, versioni, domande, risposte e criteri editoriali |
| Palestra | Catalogo degli esercizi liberi, argomenti e configurazione skill/punti |
| Orientamento | Domande standard e associazioni agli esercizi consigliati |
| Svolgimento | Tentativi, risposte, timer, salvataggi, checkpoint, consegne e giudizi |
| Progressione | Record, accrediti, totali skill, risultati corsi e prima evoluzione |
| Gamification | Accessori, equipaggiamento, aiuti, vite e attività per ottenere vite |
| Taccuino | Lezioni acquisite, annotazioni private e post preferiti |

Una vista come “I miei progressi” non implica una tabella omonima: può leggere i fatti già persistiti dagli altri domini. I DTO di lettura non sono nuove entity. I confini logici non impongono microservizi o broker di eventi.

## 3. Convenzioni tecniche concordate

- Tabelle e colonne in **inglese**, `snake_case`; tabelle plurali e riferimenti espliciti come `student_id`.
- Stati tecnici coerenti in inglese. Testi didattici e documentazione possono restare in italiano.
- Un solo modello per lo stesso fatto: niente doppio inventario accessori, doppio archivio preferiti o registri dei punti indipendenti.
- Catalogo condiviso separato dallo stato personale dello studente.
- Punti conservati con **due decimali**; la bozza usa `NUMERIC(12,2)`.
- Percentuali comprese tra 0 e 100. Il voto visualizzato è distinto dai punti e dalla percentuale usata per superare una prova.
- Tempo e identità vengono verificati dal server, non accettati come autoritativi dal client.
- Service separati da persistence e controller HTTP. Una firma di servizio non determina automaticamente GET/POST.
- Nessun controller CRUD obbligatorio per ogni tabella.
- Nessuna modifica diretta alla repository senza la proposta/approvazione richiesta dall’utente.

**Da allineare in Java:** firme TypeScript illustrative da convertire in interfacce/DTO Java; precisione decimal da mantenere nei mapping. Tipi, librerie e configurazione concreta dipendono dal progetto Spring effettivo.

## 4. Identità: dati e vincoli presenti nel SQL fornito

Quest’area non ha ricevuto nella parte visibile della conversazione un’intervista completa dei casi d’uso. Quanto segue descrive il SQL esistente; non introduce nuove regole di registrazione o consenso.

| Struttura | Vincoli/dati già rappresentati |
|---|---|
| Utente | Email normalizzata unica; hash password; dati anagrafici; stato iniziale `PENDING_VERIFICATION`; date di verifica/creazione/modifica/cancellazione |
| Ruoli | Codice univoco; assegnazione utente–ruolo unica |
| Token | Hash unico, utente, scopo, scadenza, consumo/revoca; scadenza successiva alla creazione |
| Studente | Profilo collegato a un solo utente; username unico; scuola facoltativa |
| Scuola | Dati identificativi e indirizzo |
| Genitore | Collegamento genitore–studente unico; relazione, stato e data verifica |
| Accettazioni | Documento e versione accettati dall’utente; combinazione unica |

Autorizzazioni: dati personali dello studente accessibili al proprietario; operazioni editoriali autorizzate per Admin; revisioni autorizzate per docente. Una FK verso `users` non dimostra da sola che l’utente sia docente: il servizio verifica il ruolo.

## 5. Catalogo didattico

### 5.1 Struttura e ordine

- Ogni corso appartiene a una materia.
- Ogni missione appartiene a un corso; il suo ordine è positivo e unico nel corso.
- Ogni lezione appartiene a una missione. **[Superato in v0.8]** ~~Il suo ordine è positivo e unico nella missione~~: l’ordine è quello del suo passo nella sequenza della missione (§5.5).
- Nella v0.1 il percorso è editoriale e ordinato, non personalizzato per studente.
- Il corso include titolo, descrizione breve, presentazione estesa e copertina facoltativa; l’assenza della copertina può usare un fallback UI.
- Lezioni testuali e video sono formati possibili; i video non sono condizione per far esistere la v0.1.
- I contenuti della lezione sono pagine ordinate; ogni pagina contiene esattamente un contenuto: testo, immagine o video. Formato rich text e provider video sono ancora da decidere.
- **[Superato in v0.8]** ~~Ogni lezione completa è collegata a un esercizio~~: gli esercizi sono passi della missione, non appartengono a una lezione. Una lezione si pubblica anche senza esercizio.
- Lo stesso esercizio può essere riutilizzato tra missioni, ma non duplicato nella stessa missione.
- Il riordino riceve tutti e soli gli ID del contenitore, senza duplicati, e avviene atomicamente (vincoli di ordine `DEFERRABLE`, controllati al COMMIT). Vale per missioni del corso, passi della missione e pagine della lezione.

### 5.2 Ingresso nel corso

Dal documento di dominio precedente: primo ingresso guidato da un personaggio, missione di prova, percorso in ordine nella v0.1. Test di ingresso al corso e percorsi personalizzati erano rimandati. Questo è distinto dal test facoltativo di orientamento della Palestra.

### 5.3 Ritiro e aggiornamenti

**Ultima decisione:** un contenuto ritirato non resta disponibile nel Taccuino. Il ritiro non può essere aggirato perché il contenuto era stato acquisito prima.

Il ritiro non equivale automaticamente alla cancellazione fisica di riferimenti usati in tentativi, giudizi o progressi. Nascondere il contenuto allo studente e conservarne tracce tecniche per lo storico sono responsabilità diverse.

I contenuti ancora disponibili mostrano gli aggiornamenti nel Taccuino senza perdere le annotazioni.

**[Superato in v0.6]** ~~Non confermata la proposta dei tre stati~~: gli stati sono confermati e descritti al §5.4.

**Ancora non deciso:** il comportamento sui tentativi già iniziati quando si ritira un contenuto (missione o esercizio; TODO Blocco 5) e la conservazione/visibilità delle annotazioni dopo il ritiro.

### 5.4 Stati editoriali (v0.6)

- Corsi, missioni, lezioni, esercizi del banco e voci della Palestra hanno tre stati: **bozza** (visibile solo all’Admin), **pubblicato** (visibile agli studenti, secondo il livello di accesso), **ritirato** (nascosto, conservato per lo storico).
- Un contenuto nuovo nasce in bozza. Pubblicare porta da bozza o ritirato a pubblicato; ritirare porta da pubblicato a ritirato.
- La data di prima pubblicazione resta anche dopo un ritiro e una nuova pubblicazione; la data di ritiro esiste solo se il contenuto è ritirato.
- Un contenuto mai pubblicato si può eliminare; uno già pubblicato non si elimina, si ritira.
- Lo studente vede un elemento solo se anche i suoi contenitori sono pubblicati.
- Le modifiche ai contenuti pubblicati sono ammesse e lo studente vede la versione aggiornata (gli esercizi invece cambiano tramite nuove versioni, §6.1).
- Regole di coerenza: un corso pubblicato ha almeno una missione pubblicata; una missione pubblicata ha almeno una lezione pubblicata; una lezione pubblicata ha almeno una pagina.
- L’esercizio collegato a una missione ha uno stato proprio, solo bozza o pubblicato (§5.5).

### 5.5 Sequenza della missione (v0.8)

La missione è una **sequenza ordinata di passi**; ogni passo è **una lezione oppure un esercizio**. Lezioni ed esercizi si riordinano insieme, in un unico ordine.

Regole decise il 7 novembre 2026, valutate sulla sequenza che vede lo studente (lezioni pubblicate ed esercizi pubblicati nella missione e non ritirati nel banco):

- La missione **inizia sempre con una lezione**.
- **Due lezioni di seguito** sono ammesse; **due esercizi di seguito no**.
- Per pubblicare la missione serve **almeno un esercizio**. Se l’ultimo passo non è un esercizio è solo un **avviso**, non un blocco.
- **Nessun esercizio ripetuto** nella stessa missione.
- Una lezione si pubblica anche senza esercizio.
- Il tempo dell’esercizio **parte quando l’esercizio compare** allo studente (§9.1).

Controllo “missione pronta”: l’Admin vede prima di pubblicare i **problemi** (bloccano) e gli **avvisi** (non bloccano). Oltre alle regole della sequenza, ogni esercizio deve essere pubblicato nel banco, avere una versione pubblicata e avere le skill indicate (somma 100%) oppure la skill predefinita della materia.

Missione già pubblicata: ogni modifica alla sequenza (riordino, aggiunta o rimozione di un esercizio, pubblicazione di un esercizio, ritiro di una lezione) è ammessa solo se la sequenza vista dallo studente resta valida; altrimenti viene rifiutata con il motivo.

Esercizio nella missione:

- Nasce **in bozza**; le skill si possono indicare già quando lo si aggiunge.
- In una missione **non ancora pubblicata**, gli esercizi in bozza vengono pubblicati **insieme alla missione**.
- In una missione **già pubblicata**, l’esercizio aggiunto resta **nascosto** finché l’Admin non lo pubblica; per pubblicarlo deve essere pronto (esercizio e versione pubblicati, skill indicate o skill predefinita della materia).
- Non ha uno stato “ritirato”: per nasconderlo si ritira l’esercizio nel banco oppure lo si scollega. Si scollega solo se non è mai stato svolto.
- Tempo, punti massimi e skill si possono cambiare anche dopo i primi tentativi: ogni tentativo conserva i valori con cui è stato svolto.

**Rappresentazione SQL:** `mission_steps` con esattamente uno tra lezione ed esercizio, ordine unico e `DEFERRABLE` nella missione, FK composte che garantiscono che lezione ed esercizio appartengano alla stessa missione del passo. `mission_exercises` non ha più `lesson_id`; `lessons` non ha più `sort_order`.

### 5.6 Livello di accesso (freemium)

- Il corso ha un livello free o premium (default premium); la missione può non indicarlo (eredita dal corso) oppure sovrascriverlo.
- Decisione del 5 novembre 2026: **ammesse missioni premium anche in corsi free** (lucchetto sulla missione). Lo studente vede il corso pubblicato; il controllo premium è sulla missione.
- Lezioni ed esercizi della missione ereditano dalla missione. Le voci della Palestra hanno un livello proprio (default premium).
- **Aperto:** completamento del corso e prima evoluzione quando il corso contiene missioni premium.

### 5.7 Area Admin e impostazioni dell’Academy (v0.6, v0.7)

- Le operazioni editoriali sono riservate al ruolo ADMIN; le API Admin del catalogo sono sotto `/admin/academy`.
- Corsi, missioni, lezioni, esercizi e versioni registrano **data di creazione** e **data di ultima modifica**; modificare le pagine aggiorna la data della lezione. Le versioni registrano anche quando hanno smesso di essere quella pubblicata, così ogni versione mostra “in uso dal … al …”.
- Impostazioni dell’Academy (riga unica, modificabili): soglia di superamento **60%**, fattore skill delle missioni **10%**, tempo predefinito dell’esercizio **10 minuti**, punti massimi predefiniti **10**. I valori predefiniti vengono copiati nell’esercizio quando lo si collega: cambiarli dopo non riscrive gli esercizi esistenti né lo storico.
- Le interfacce Admin non usano termini tecnici e confermano visibilmente ogni salvataggio.

## 6. Esercizi: contenuti e valutazione

### 6.1 Identità, versioni e riutilizzo

- L’esercizio ha un’identità editoriale distinta dalla versione del contenuto e dal tentativo dello studente.
- La bozza conserva numero di versione unico per esercizio.
- I contratti prevedono che un nuovo tentativo risolva la versione disponibile e conservi ciò che sta svolgendo.
- Conservare versioni usate ed evitare riscritture dello storico è il modello tecnico proposto. **[Aggiornato in v0.6]** Tempo, massimi e pesi dell’esercizio nella missione si possono cambiare anche dopo l’uso: ogni tentativo conserva i propri valori (tempo concesso e configurazione salvati nel tentativo).
- Le letture editoriali possono includere soluzioni; quelle destinate allo studente non le espongono indiscriminatamente prima della consegna.

**Versione pubblicata (v0.6):**

- L’Admin lavora su nuove versioni in bozza; lo studente svolge **solo la versione pubblicata**, al massimo una per esercizio.
- Una versione pubblicata non si modifica: per correggere si crea una nuova versione e la si pubblica al posto della precedente.
- Ogni tentativo registra la versione svolta, quindi lo storico non dipende dalla versione attuale.
- Un esercizio si pubblica solo se ha una versione pubblicata; le bozze di versione mai pubblicate si possono eliminare.

**Ritiro di un esercizio dal banco (v0.8):**

- Il ritiro è **sempre permesso** e deve essere rapido, anche se l’esercizio è usato in missioni pubblicate o pubblicato in Palestra. L’esercizio sparisce da missioni e Palestra.
- Prima del ritiro l’Admin vede **dove è usato l’esercizio**; se una missione resterebbe senza esercizi, il frontend mostra un avviso prima di procedere. Nessun blocco.
- Nel controllo “missione pronta” l’esercizio ritirato compare come avviso; la missione rimasta senza esercizi risulta non pronta.
- **TODO:** tentativi già avviati su un esercizio ritirato (Blocco 5); nascondere nella Palestra dello studente le voci con esercizio ritirato (Blocco 4).

**Valutazione (v0.6):** la modalità è sulla domanda, quindi versionata. Scelta multipla e completamento sempre automatici; traduzione di versione sempre docente; traduzione di frase a scelta dell’autrice (alternative accettate oppure correzione docente). Le rubriche delle traduzioni di versione sono una guida per la docente, che assegna percentuale e feedback complessivi.

### 6.2 Tipologie

| Tipo | Regole raccolte |
|---|---|
| Scelta multipla | Una sola risposta corretta; feedback per opzione facoltativo; spiegazione generale; opzioni mescolate nei nuovi tentativi |
| Completamento | Un solo spazio; teoria grammaticale; possibili risposte alternative; confronto senza differenze di maiuscole e spazi esterni |
| Traduzione di frase | Consegna distinta dal testo originale; soluzione e spiegazione editoriali; versione con alternative prevista, traduzione libera secondo il perimetro contenuti |
| Traduzione di versione | Prova finale prevista; rubriche distinguono errori strutturali gravi e scelte lessicali contestuali; esito docente quando richiesto |
| Rielaborazione | Lo studente rispiega una regola con parole proprie scegliendo badge/contesto, per esempio uno sport; completamento associato determina il superamento |

Marcatore dello spazio, formato rich text, rappresentazione rubriche e mescolamento persistito sono convenzioni implementative, non nuovi comportamenti da introdurre.

### 6.3 Correzione docente

- La docente assegna **percentuale 0–100 e feedback scritto**.
- Dopo la convalida, il giudizio è **definitivo nella v0.1**.
- L’attesa non produce una valutazione provvisoria definitiva né perdita di vite.
- L’invio blocca la modifica della risposta; la possibilità di annullare l’invio è trattata separatamente sotto.

## 7. Palestra

### 7.1 Catalogo

- È sempre accessibile, anche senza vite.
- Ogni esercizio compare una sola volta nel catalogo Palestra.
- Ogni esercizio ha **una skill e un argomento**.
- I punti massimi sono impostati dall’autrice/Admin.
- La materia si ricava dall’argomento; il titolo dall’esercizio editoriale.
- Latino e Greco possono avere argomenti/esercizi diversi.
- Sono ammessi esercizi solo Palestra, senza missione.
- La skill in Palestra può differire dalla ripartizione skill dello stesso contenuto usato in Missione.
- Filtri previsti: materia, argomento, skill.
- Nessun aiuto da equipaggiamento negli esercizi Palestra nella v0.1.
- **(v0.6)** La voce Palestra ha i tre stati editoriali e un proprio livello di accesso free/premium.
- **(v0.8)** Se l’esercizio viene ritirato dal banco, la voce sparisce anche dalla Palestra, senza dover ritirare prima la voce.

### 7.2 Tentativi e salvataggi

| Azione | Effetto |
|---|---|
| Avvio | Nessun requisito o consumo di vite |
| Esce senza salvare | Abbandona l’esercizio; nuovo avvio da zero |
| Salva esercizio | Conserva risposta e punto raggiunto |
| Riprende | Recupera la bozza salvata |
| Consegna alla docente | Risposta bloccata fino all’esito |
| Attende correzione | Può fare altri esercizi, non ritentare lo stesso |

Più esercizi possono essere salvati contemporaneamente, con una sola bozza per ciascun esercizio. Il timer Palestra è opzionale e non determina la valutazione.

La successiva indicazione “Annulla invio” è stata formulata nel contesto Missione: non si estende automaticamente alla Palestra senza specifica.

## 8. Test preliminare di orientamento

- Strumento facoltativo per chi non sa cosa allenare.
- Distinto per materia, ripetibile liberamente, senza timer.
- Domande standard e risposte selezionabili; associazioni agli esercizi preimpostate dall’autrice.
- In base alle scelte seleziona **massimo cinque esercizi disponibili**.
- Elimina i duplicati; i consigli appartengono alla materia del test.
- Lo studente decide liberamente se svolgerli.
- Nessun punto, vita, skin, revisione docente o autovalutazione nel test.
- Nessuno storico dei test né persistenza delle risposte studente nella v0.1.
- Persistono soltanto configurazione, domande, opzioni e associazioni agli esercizi.
- Tracciamento per personalizzazione rimandato; eventuali click per misurare utilità sono una possibilità, non un requisito.

**Proposte tecniche:** una scelta per domanda; una configurazione per materia; priorità minima fra le associazioni, poi ID a parità. L’ordine numerico è il criterio semplice discusso; obbligatorietà di tutte le risposte e dettaglio degli spareggi non sono decisioni aggiuntive confermate.

Le traduzioni e il feedback docente riguardano gli esercizi consigliati, non le domande di questo test.

## 9. Missioni: tentativi, timer e checkpoint

### 9.1 Accesso e sequenza

- Almeno **una vita** per avviare, ritentare o riprendere una missione.
- L’ingresso non consuma la vita: si perde solo al fallimento.
- Una sola missione in corso per studente, inclusa una missione salvata.
- Lezioni ed esercizi uno alla volta nell’ordine della sequenza (§5.5); dopo un esercizio si procede solo dopo il suo superamento.
- Ogni esercizio ha il proprio tempo configurato.
- **[Superato in v0.8]** ~~Il timer parte quando termina la lezione testuale o video precedente~~: il tempo parte **quando l’esercizio compare** allo studente. Poiché la missione inizia con una lezione e due esercizi non si susseguono, ogni esercizio compare dopo una lezione.
- Non è stata approvata una schermata obbligatoria “Continua”; la proposta successiva di renderla necessaria non va usata come requisito.
- Alla consegna il timer si ferma, anche in attesa della docente.
- Il tempo non modifica la percentuale di correttezza. Eventuali premi velocità richiedono formula distinta, ancora da consolidare.

### 9.2 Salvataggio, uscita e fallimento

| Evento | Esito | Vite | Ripartenza |
|---|---|---:|---|
| Salva missione | Salvata | Nessuna perdita | Stessa risposta e tempo residuo |
| Chiude la pagina | Salvataggio automatico | Nessuna perdita per la sola uscita | Stesso punto/tempo residuo |
| Esercizio valutato sotto 60% | Tentativo fallito | −1 | Esercizio fallito, risposta vuota e tempo completo |
| Tempo scaduto | Tentativo fallito | −1 | Esercizio corrente, risposta vuota e tempo completo |
| Abbandona missione prima dell’invio finale | Tentativo fallito | −1 | Missione da zero |

Gli esercizi già superati sono checkpoint conservati nei ritentativi dopo errore o scadenza. L’abbandono esplicito comporta un nuovo percorso da zero. Una sola vita persa per tentativo fallito, anche se più condizioni arrivano contemporaneamente.

La risposta precedente rimane nei progressi/correzione. Dopo salvataggio si riprende la stessa esecuzione; dopo fallimento si crea un nuovo tentativo mantenendo i checkpoint appropriati.

**Nota tecnica:** il salvataggio automatico alla chiusura va realizzato con sincronizzazione e rilevamento della disconnessione; non basta promettere che il browser invierà sempre un evento finale. La politica temporale precisa resta implementativa.

### 9.3 Superamento

- Risultato esercizio Missione: **media ponderata delle skill** coinvolte.
- Soglia: **60% del risultato ponderato**, senza imporre 60% separatamente a ogni skill (regola recuperata nel draft Palestra). **(v0.6)** La soglia è nelle impostazioni dell’Academy; ogni giudizio conserva la soglia usata.
- Il percorso ordinario richiede il superamento degli esercizi della missione.
- Un nuovo fallimento in una ripetizione non cancella il superamento già ottenuto né il miglior record.
- Il dettaglio per skill resta distinto dal risultato complessivo.

### 9.4 Consegna finale e “Annulla invio”: ultima correzione

L’utente ha precisato: quando invia alla docente la missione è finita sul piano dello svolgimento; non può usare “Abbandona missione”. Questo **non significa** che sia già superata prima del giudizio.

Si prevede **“Annulla invio”**. In quel caso l’esercizio è considerato non superato, **ma la missione non viene considerata fallita per quell’annullamento**.

Questa è un’eccezione esplicita alla precedente equiparazione automatica esercizio non superato → missione fallita. Le vecchie firme e gli stati SQL non la rappresentano ancora completamente.

**Non deciso:** perdita o meno di vite, punto/stato di ripresa, trattamento di un giudizio già emesso e collocazione delle consegne docente prima dell’ultimo esercizio. Le proposte successive su questi aspetti sono state ritirate; il blueprint non le inventa.

## 10. Progressione: punti, record e risultati

### 10.1 Palestra

La soglia per ottenere punti è **60%**. Sotto soglia: risultato nello storico, senza accredito.

Formula confermata:

`contributo esercizio = punti massimi × percentuale definitiva / 100`

Conservare il contributo con due decimali. Un nuovo record accredita soltanto la differenza positiva rispetto al contributo già acquisito. Prestazioni uguali o peggiori non accreditano altri punti.

| Esercizio massimo 10 | Contributo | Nuovo accredito |
|---|---:|---:|
| Primo risultato 87% | 8,70 | +8,70 |
| Record 89% | 8,90 | +0,20 |
| Risultato 80% | Record resta 8,90 | 0 |
| Record 100% | 10,00 | +1,10 |

Il totale Palestra di una skill è la somma dei migliori contributi dei suoi esercizi. Esempio Grammatica: 8,70 + 6,50 + 10,00 = **25,20**. Le ripetizioni non consentono di accumulare illimitatamente sullo stesso massimo.

### 10.2 Voto visualizzato

Il voto mostrato è arrotondato al **mezzo punto più vicino**. I punti rimangono a due decimali.

| Voto calcolato | Mostrato |
|---|---:|
| 8,00–8,24 | 8 |
| 8,25–8,74 | 8,5 |
| 8,75–9,00 | 9 |

Esempi: 8,3 → 8,5; 8,7 → 8,5; 8,8 → 9. Nessun trasferimento di frazioni tra skill. Il voto arrotondato non decide superamento, record o punti.

**Proposta tecnica:** arrotondamento decimal alla seconda cifra con metà verso l’alto. È distinta dalla regola confermata del voto visuale e va uniformata nel codice.

### 10.3 Missioni: skill e skin

Regole recuperate nel SQL Palestra e riconosciute nella conversazione:

- Massimo punti dell’esercizio e ripartizione percentuale fra le skill.
- Valutazioni per skill; risultato complessivo ponderato.
- Punti skill Missioni con fattore **10%**, configurabile.
- I punti skill maturati restano anche quando la missione non viene superata.
- Punti skin: somma dei punti degli esercizi, accreditata **solo al superamento della missione**, senza riaccredito nelle ripetizioni.

Esempio del fattore: un contributo skill equivalente a 8,70 produce **0,87** con fattore 10%.

Le domande successive che proponevano di accreditare tutte le skill soltanto al superamento o di applicare automaticamente la regola differenza delle Missioni sono state ritirate: non costituiscono conferma.

**Da allineare tecnicamente:** identificazione della sorgente dell’accredito skill per esercizio Missione, regola dei ritentativi/record skill Missioni e registro punti skin. Il SQL consolidato e i vecchi metodi di solo superamento Missione non coprono ancora tutto questo comportamento.

**Rappresentazione attuale (v0.6, provvisoria):**

- L’esercizio nella missione ripartisce il risultato fra le skill con pesi in percentuale, **somma 100**. Nessun peso indicato = 100% sulla **skill predefinita della materia**, se la materia ne ha una.
- Il fattore skill delle missioni (10%) è nelle impostazioni dell’Academy; ogni contributo conserva il fattore usato. Cambiare i pesi vale per i contributi calcolati da quel momento.
- Oggi la mancanza di skill (senza skill predefinita della materia) **blocca** la pubblicazione dell’esercizio nella missione e della missione.

**TODO (gamification) — da ricontrollare:** le regole delle skill non sono ancora chiare. Quando si sviluppa la progressione vanno riviste: se le skill devono bloccare la pubblicazione, il fallback sulla skill predefinita della materia, il significato dei pesi e il calcolo dei contributi. Lo stesso TODO è nel codice (`IMissionExercisesService`, `MissionsService.exerciseProblems`).

### 10.4 Corso e prima evoluzione

- La media del corso usa i migliori risultati delle missioni, con lo stesso peso per ogni missione secondo la regola precedente non riaperta.
- Un record migliore aggiorna la media; un risultato peggiore non la abbassa.
- Corso completato quando tutte le missioni richieste sono superate.
- **Solo nella v0.1:** prima evoluzione al completamento del primo corso, senza richiedere 100% in ogni missione.
- Premio: cambio di personaggio e base vite **da 5 a 7**.
- Saldo immediato portato almeno a 7, conservando gli extra.
- Timer della ricarica già attivo invariato.
- Prima evoluzione una sola volta; una richiesta/evento duplicato non assegna di nuovo il premio.

| Prima | Dopo |
|---|---|
| Base 5, saldo 3 | Base 7, saldo 7 |
| Base 5, saldo 9 | Base 7, saldo 9 |

Soglie livelli skill, evoluzioni ulteriori e conversioni dei punti evoluzione future non vengono fissate da questa eccezione. Il vecchio arrotondamento sempre per difetto a scaglioni 5% non va usato per sostituire la più recente regola del voto mostrato.

### 10.5 “I miei progressi”

Contenuti confermati per Palestra: storico dei tentativi valutati anche sotto60%, date, risposte, percentuali e feedback; record personale; accrediti per esercizio; totali skill; esercizi in revisione distinti dai risultati definitivi.

Le risposte fallite restano consultabili. Gli orari persistiti possono supportare analisi future; algoritmi di consiglio sugli orari/errori non diventano obbligatori solo perché il dato è conservato.

## 11. Accessori, equipaggiamento e aiuti

### 11.1 Sblocco e riscatto

- Cappello da turista: riscattabile a **50 punti Grammatica**.
- Riscatto manuale tramite **“Riscatta”**.
- Nessun consumo di punti; possesso permanente.
- Non equipaggiato automaticamente: lo studente sceglie di indossarlo.
- Per altri accessori, soglia iniziale 100 punti della skill richiesta come riferimento configurabile v0.1; non soglia universale definitiva.
- Distinguere riscattabile, posseduto ed equipaggiato.

### 11.2 Equipaggiamento

- Ogni accessorio ha uno spazio, per esempio testa o corpo.
- Un solo accessorio per spazio; il nuovo sostituisce quello indossato.
- Cambio consentito fuori dal tentativo Missione.
- Togliere/rimettere non ripristina cariche e non azzera la ricarica.
- Il divieto durante una missione salvata era un’interpretazione tecnica da precisare, non una regola aggiuntiva approvata separatamente.

### 11.3 Aiuto del cappello

| Aspetto | Regola |
|---|---|
| Tipo | Aiuto grammaticale |
| Capacità | 5 utilizzi complessivi |
| Ambito | Solo Missioni nella v0.1 |
| Condivisione | Stesso saldo tra tutti i corsi/missioni dello studente |
| Requisito | Accessorio indossato per usare un nuovo aiuto |
| Nuovo aiuto | Consuma una carica |
| Stesso aiuto nello stesso tentativo | Riapertura gratuita |
| Più aiuti nella stessa missione | Consentiti, gestendo le5 cariche |
| Ricarica | Completa12 ore dopo l’esaurimento dell’ultima carica |

Aiuti generali riutilizzabili; non è stato richiesto un catalogo di suggerimenti specifici per ogni domanda. La carica non viene consumata una seconda volta perché il client ripete la richiesta.

**Proposte tecniche:** cariche iniziali complete al primo riscatto; un beneficio per accessorio nel modello v0.1. Disattivazione/modifiche di accessori già posseduti da gestire nella relativa implementazione.

## 12. Vite e attività per ottenere vite

### 12.1 Saldo e ricarica

- Base iniziale5, condivisa fra tutti i corsi.
- Prima evoluzione v0.1 porta la base individuale a7.
- Una vita persa per tentativo Missione fallito; mai un consumo aggiuntivo alla chiusura dello stesso fallimento.
- Salvataggio/ripresa e semplice chiusura della pagina Missione non perdono vite.
- Dieci ore dalla **prima vita persa**, senza riavvio del timer alle perdite successive.
- Alla scadenza: saldo portato **almeno alla base personale**, conservando le vite extra.
- Non attendere zero vite per iniziare la ricarica.
- A zero restano disponibili Palestra e consultazione dei contenuti disponibili; Missioni richiedono almeno una vita.

Esempio: base5/saldo3 → alla ricarica5; base5/saldo7 →7. Dopo evoluzione base7/saldo3 →7; base7/saldo9 →9.

### 12.2 Attività dedicata

- Separata da esercizi Palestra e Missioni.
- **Sempre accessibile**, ultima conferma del9 ottobre; non richiede timer attivo o zero vite.
- Domanda con spiegazione libera dello studente.
- V0.1: confronto con soluzione guidata e autovalutazione.
- Un completamento riuscito assegna **una vita**, senza punti skill/skin o record Palestra.
- Ripetibile liberamente, senza limite ai guadagni; può superare la base5/7.
- Difficoltà aumenta con le vite ottenute tramite l’attività.
- Difficoltà si azzera alla scadenza della ricarica delle10 ore.
- Vite guadagnate non riavviano la ricarica.
- Stesso completamento non accredita più di una vita, neppure con doppio invio.

**Non deciso:** scelta numerica dei livelli, comportamento quando finiscono le domande/livelli preparati, abbandono dell’attività e azzeramento difficoltà se si ottengono vite senza mai avviare un timer di perdita. La proposta di mantenere il livello massimo non è stata confermata. Non introdurre un nuovo timer per risolvere implicitamente questi casi.

## 13. Rielaborazione e AI

- Lo studente rielabora la regola in italiano tramite esempi o paragoni con un contesto conosciuto, anche sport.
- AI desiderata **soltanto come correttore della rielaborazione dei contenuti**.
- Modalità scelta: AI propone correzione/feedback/valutazione; docente convalida o modifica.
- Il completamento associato determina il superamento dell’esercizio.
- La missione non attende questa convalida della rielaborazione per il superamento.
- Giudizio della rielaborazione non modifica superamento, punti o vite.
- Non estendere implicitamente AI a traduzioni, quiz o attività vite.
- Integrazione da verificare nel progetto, non già implementata né promessa entro una data.

**Aperto:** visibilità allo studente della proposta AI prima della convalida. La proposta “solo docente fino a convalida” non ha ricevuto conferma; la domanda è stata contestata e ritirata.

## 14. Taccuino

| Contenuto | Ingresso | Uscita/visibilità |
|---|---|---|
| Lezione | “Ho finito di leggere”; completamento del video se previsto | Non eliminabile volontariamente dallo studente; contenuto ritirato rimosso anche dal Taccuino |
| Nota della lezione | Annotazione privata dello studente | Modificabile/cancellabile; non condivisa nella v0.1 |
| Post Blog | Aggiunta ai preferiti | Rimozione preferito rimuove la voce Taccuino |

- Stessa lezione acquisita una sola volta, anche se riutilizzata.
- Contenuti disponibili mostrati aggiornati; le annotazioni restano quando il contenuto viene aggiornato.
- Risposte, voti, errori e correzioni appartengono ai progressi, non al Taccuino.
- Note Blog posticipate; condivisione docente fuori v0.1.
- Fonte unica per preferiti Blog, non due elenchi da sincronizzare.
- Dal documento precedente: filtri materia/corso/missione ed elenco recente, senza ricerca avanzata.
- Il suggerimento Taccuino dopo due fallimenti era previsto come consiglio, non blocco ai ritentativi; posizionamento/contatore da riconciliare in implementazione.

**Proposte tecniche:** unico riquadro note per lezione, autosalvataggio, versione per evitare sovrascritture concorrenti. **Non deciso:** cosa conservare delle note dopo il ritiro del contenuto e trattamento dei preferiti relativi a un post non disponibile.

## 15. Invarianti trasversali e metodi

- Unicità del possesso studente–accessorio, del preferito studente–post e dell’acquisizione studente–lezione.
- Una sola missione in corso, una sola bozza/revisione Palestra per lo stesso esercizio.
- Checkpoint di esercizi realmente superati dello stesso percorso/studente.
- Una perdita di vita per tentativo; un premio vita per completamento; una prima evoluzione per studente.
- Accrediti definitivi coordinati con record/stato, senza doppio conteggio.
- Giudizi definitivi immutabili nella v0.1.
- Autenticazione e controllo proprietà nei servizi; le FK verificano esistenza, non autorizzazione.
- Operazioni che cambiano più fatti correlate devono essere atomiche o usare una consegna affidabile e deduplicata.
- Stessa richiesta non deve generare doppi effetti. Chiave ripetuta con dati incompatibili produce conflitto.
- Saldi, cariche e tempi non sono setter arbitrari esposti allo studente.
- Le scadenze sono verificate dal server; non serve necessariamente un job per studente.
- **(v0.8)** Ogni lezione ed esercizio della missione è esattamente un passo della sua sequenza; le regole sulla sequenza (§5.5) coinvolgono più righe e le garantisce il service.
- **(v0.6)** Al massimo una versione pubblicata per esercizio; una versione pubblicata non si modifica.

Le interfacce raccolte coprono Catalogo, Esercizi, Palestra, Orientamento, Gamification, Svolgimento, Correzione, Progressione e Taccuino:19 interfacce con102 metodi nella raccolta precedente. Sono contratti progettuali, non102 endpoint obbligatori.

**Limite:** gli aggiornamenti più recenti, in particolare Annulla invio, richiedono correzioni delle firme e degli stati. L’elenco esistente non va implementato alla cieca.

## 16. Decisioni superate: evitare regressioni

| Vecchio riferimento | Regola corrente |
|---|---|
| Cappello dato subito o soglia100 | Soglia50 Grammatica, riscatto manuale |
| Un aiuto per missione | Più utilizzi possibili entro cariche disponibili |
| Cariche del cappello per corso | Cariche condivise tra corsi e missioni |
| Ricarica vite12h da esaurimento |10h dalla prima perdita |
| Sempre base5 | Base personale5, poi7 dopo evoluzione v0.1 |
| Uscita browser Missione = fallimento | Salvataggio automatico senza perdita per sola uscita |
| Timer parte con Continua obbligatorio | Parte alla fine della lezione precedente |
| Punti interi/per difetto | Punti con2 decimali; voto visuale al mezzo punto |
| Missioni non danno skill | Fattore10%; skill conservate anche in missione non superata |
| Esercizio annullato = missione fallita | Annulla invio: esercizio non superato, missione non fallita per questo |
| Contenuto ritirato resta nel Taccuino | Ritirato anche dal Taccuino |
| Recupero solo a zero/durante attesa | Attività sempre accessibile |
| Test orientamento con storico/correzione docente | Domande standard, consigli, nessuno storico o revisione |
| Stati Bozza/Pubblicato/Ritirato non confermati | Confermati (v0.6, §5.4) |
| Ogni lezione completa collegata a un esercizio | Sequenza libera: l’esercizio è un passo della missione; lezione pubblicabile senza esercizio (v0.8) |
| Esercizio della lezione (`mission_exercises.lesson_id`) | Esercizio della missione, posizione nel suo passo (`mission_steps`, v0.8) |
| Ordine delle lezioni = ordine degli esercizi | Un unico ordine di passi, lezioni ed esercizi insieme (v0.8) |
| Timer dalla fine della lezione precedente | Il tempo parte quando l’esercizio compare (v0.8) |
| Esercizio collegato visibile subito | Nasce in bozza; esce con la missione o, se la missione è già pubblicata, quando lo si pubblica (v0.8) |
| Ritiro dal banco bloccato se l’esercizio è usato in lezione/missione pubblicata o in Palestra | Ritiro sempre permesso, con avviso su dove è usato (v0.8) |
| Missione premium non ammessa in corso free (commento della migrazione v0.6) | Ammessa, lucchetto sulla missione (decisione del 5 novembre) |
| Politica su timer/massimi/pesi dopo l’uso non decisa | Modificabili: ogni tentativo conserva i suoi valori (v0.6) |

## 17. Stato del SQL e del codice

La bozza inglese contiene57 tabelle e una vista dei totali skill. Il precedente controllo ha verificato staticamente94 FK e5 indici unici parziali; parsing riuscito. Questo non sostituisce l’esecuzione su PostgreSQL o i test dei servizi.

Il SQL rappresenta molte regole ma manca ancora di alcuni dati/cataloghi, fra cui disponibilità, timer/pesi/rubriche editoriali, sorgenti aiuti/Blog e parte dei premi/accrediti Missioni. Le ultime correzioni non sono state applicate automaticamente ai vecchi file.

Si prosegue implementando il backend Java per blocchi e correggendo gli allineamenti in itinere. Il linguaggio TypeScript delle interfacce era illustrativo; non cambia le regole del backend Java. Nessuna repository o database è stata modificata per produrre questo blueprint.

**Stato al 7 novembre 2026 (v0.2):**

- Migrazioni `bstudent_migration_v0.2` … `v0.8` applicate al DB di sviluppo; transazionali e rieseguibili. Hibernate valida lo schema all’avvio.
- Backend Admin del catalogo completo e provato: materie, corsi, missioni con sequenza, lezioni e pagine, banco esercizi con versioni, esercizi della missione con tempo, punti e skill, catalogo skill.
- Frontend Admin minimo nel repo dell’Academy; da adeguare alla sequenza della v0.8.
- Ancora da fare: letture dello studente (Blocco 4), svolgimento delle missioni (Blocco 5), progressione e gamification.

**TODO aperti nel codice:** anteprima Admin e “In arrivo”; accesso alla pagina corso; completamento del corso con missioni premium; assegnazione ruoli; formato del testo e provider video delle pagine; tentativi su contenuti ritirati (Blocco 5); Palestra senza esercizi ritirati (Blocco 4); regole delle skill (gamification, §10.3).

## 18. Fonti e priorità

Fonti lette per il consolidamento:

1. Conversazione fino alla scelta Java e alle precisazioni del9 ottobre, prioritaria sulle bozze precedenti.
2. BStudent_Core_English_v0.1.sql — versione salvata9 ottobre.
3. BStudent_Interfacce_Core_Parte1_v0.1.md —8 ottobre.
4. BStudent_Interfacce_Gamification_Svolgimento_v0.1.md —8 ottobre.
5. BStudent_Interfacce_Progressione_Taccuino_v0.1.md —9 ottobre.
6. BStudent_Palestra_attributi_draft_v0.1.sql —6 ottobre, incluse le regole Missioni recuperate.
7. BStudent_Regole_e_Domini_v0.1.docx —29 settembre, soltanto dove non superato da decisioni successive.
8. BStudent_Svolgimento_v0.1.sql —8 ottobre.
9. **(v0.2)** Decisioni prese durante lo sviluppo, dal 30 ottobre al 7 novembre 2026, e migrazioni `bstudent_migration_v0.6.sql`, `v0.7.sql`, `v0.8.sql`: prevalgono sulle fonti precedenti.

Questo blueprint consolida le regole; gli allegati SQL/interfacce restano bozze da allineare. I punti indicati come aperti non impongono una nuova intervista completa né vengono convertiti silenziosamente in funzionalità.

## Appendice — Mappa completa delle strutture SQL

I vincoli sotto descrivono la bozza salvata; non attestano che tutte le regole del blueprint siano già implementate. Le strutture future possono restare senza nuovi metodi obbligatori.

| Tabella inglese | Fatto/dato rappresentato |
|---|---|
| `users` | Identità account, dati anagrafici, stato e date di verifica |
| `roles` | Catalogo ruoli con codice univoco |
| `user_roles` | Ruoli assegnati all’utente, coppia unica |
| `auth_tokens` | Token hash/scopo/scadenza/consumo/revoca |
| `schools` | Dati scuola e indirizzo |
| `students` | Profilo studente, utente e username unici, scuola facoltativa |
| `guardian_links` | Legame genitore–studente, stato verifica |
| `policy_acceptances` | Accettazione documento/versione, combinazione unica per utente |
| `skills` | Catalogo skill, codice/nome/descrizione/stato |
| `skill_levels` | Livello e soglia punti unici nella skill |
| `skins` | Catalogo personaggi/skin, codice univoco |
| `skin_evolutions` | Stadi della skin, numero unico nella skin |
| `student_skins` | Skin possedute, una per coppia studente–skin |
| `student_skin_evolutions` | Stadi acquisiti compatibili con la skin posseduta |
| `character_selections` | Una selezione corrente per studente, riferita a possesso e stadio acquisito |
| `subjects` | Materia e nome non vuoto; skill predefinita facoltativa (v0.6) |
| `courses` | Materia, titolo e descrizioni, copertina facoltativa; livello di accesso, stato e date editoriali |
| `missions` | Corso, titolo/descrizione e ordine unico; livello di accesso facoltativo, stato e date editoriali |
| `lessons` | Missione e titolo, stato e date editoriali; **(v0.8)** nessun ordine proprio |
| `lesson_pages` | Blocchi ordinati: testo/immagine/video con campi coerenti col tipo |
| `mission_steps` | **(v0.8)** Passo della missione: lezione oppure esercizio, ordine unico e `DEFERRABLE` nella missione |
| `question_bank_entries` | Identità esercizio e titolo editoriale, stato e date editoriali |
| `question_versions` | Numero versione univoco nell’esercizio; al massimo una pubblicata; date di pubblicazione e sostituzione (v0.6, v0.7) |
| `questions` | Una domanda per versione, tipo/consegna/testo/spiegazione, modalità di valutazione (v0.6) |
| `question_answers` | Risposte/soluzioni, ordine univoco e flag correttezza |
| `question_rubric_criteria` | **(v0.6)** Criteri di rubrica ordinati delle traduzioni di versione: strutturali o lessicali |
| `mission_exercises` | Esercizio della missione: tempo, punti massimi, stato bozza/pubblicato; non duplicato nella missione; **(v0.8)** non più legato a una lezione |
| `mission_exercise_skills` | **(v0.6)** Pesi skill dell’esercizio nella missione, fra 0 e 100 (somma 100 garantita dal service) |
| `academy_settings` | **(v0.6)** Riga unica: soglia di superamento, fattore skill, tempo e punti predefiniti |
| `topics` | Argomento della materia, nome unico nella materia |
| `practice_exercises` | Una associazione Palestra per esercizio, skill/argomento/massimo positivo; stato e livello di accesso (v0.6) |
| `gamification_settings` | Configurazione iniziale singleton, base5 e ricarica10h |
| `accessories` | Codice/spazio/skill/soglia positiva e disponibilità |
| `student_accessories` | Possesso riscattato una sola volta per studente–accessorio |
| `equipped_accessories` | Un accessorio per spazio/studente, possesso e spazio coerenti |
| `accessory_benefits` | Proposta un beneficio per accessorio, capacità/durata positive |
| `accessory_charge_states` | Saldo cariche e ricarica; a zero scadenza obbligatoria |
| `hint_uses` | Un aiuto ottenuto per studente/tentativo/aiuto |
| `student_life_states` | Saldo non negativo, base personale positiva, scadenza e contatore recuperi |
| `mission_life_losses` | Una perdita registrata per tentativo Missione |
| `life_recovery_activities` | Domanda/soluzione, difficoltà positiva, disponibilità |
| `life_recovery_attempts` | Risposta/autovalutazione/data premio; una esecuzione aperta per studente |
| `mission_runs` | Percorso con checkpoint; uno in corso per studente |
| `mission_attempts` | Tentativi numerati nel percorso, un tentativo aperto, motivo fallimento/data coerenti |
| `exercise_attempts` | Contesto Missione/Palestra, risposta/configurazione/tempo e stato; unicità tentativi aperti |
| `exercise_assessments` | Un giudizio per tentativo, percentuale/soglia0–100, passed derivato, docente quando necessario |
| `mission_checkpoints` | Un checkpoint per percorso/voce, riferito a giudizio |
| `content_reformulation_reviews` | Testo/contesto, proposta AI e convalida separata |
| `practice_records` | Miglior contributo qualificato, uno per studente/esercizio Palestra |
| `mission_skill_contributions` | Contributo Missione per studente/missione/skill: struttura da completare per esiti esercizi |
| `skill_credit_entries` | Registro crediti decimal, delta positivo, evento/skill unico, nuovo=precedente+delta |
| `mission_records` | Miglior risultato missione, uno per studente/missione |
| `course_results` | Missioni superate/totali e risultato, completamento solo con tutte superate |
| `student_first_evolutions` | Una prima evoluzione per studente; evento unico |
| `notebook_lessons` | Acquisizione unica per studente/lezione |
| `lesson_notes` | Nota privata della lezione acquisita; testo non vuoto e versione positiva |
| `blog_post_favorites` | Preferito unico per studente/post, riferimento Blog da integrare |
| `orientation_tests` | Un test per materia nella rappresentazione proposta |
| `orientation_questions` | Domande ordinate del test, ordine unico |
| `orientation_options` | Opzioni ordinate della domanda, ordine unico |
| `orientation_recommendations` | Associazione opzione–esercizio Palestra unica, priorità positiva |

`student_skill_totals` è una **vista**, non una tabella: somma i crediti per studente, skill e origine. I totali non sono una seconda fonte di accredito.
