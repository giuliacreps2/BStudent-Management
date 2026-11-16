# BStudent — Blueprint della gamification v0.1

Iniziato il 16/11/2026 (branch `feature/gamification-development`). Raccoglie le regole di gioco decise con
Giulia: livelli delle skill, difficoltà degli esercizi, XP, evoluzioni, personaggi, accessori, vite, sfida
giornaliera e streak.

Il blueprint principale (`BStudent_Blueprint_Regole_SQL_v0.2.md`, §10–§12) resta la fonte delle regole già
decise prima di questo documento; qui si aggiungono le decisioni nuove. Dove i due documenti parlano della
stessa cosa, **vale questo**, perché è più recente.

## 0. Come leggere questo documento

- **Deciso** = confermato da Giulia, con la data.
- **Da decidere** = domanda ancora aperta, con la proposta.
- **TODO dopo la beta** = scelta rimandata di proposito: non va sviluppata ora.
- **Tutti i numeri sono un punto di partenza.** Soglie, punti e livelli stanno in tabelle modificabili
  dall'Admin, senza toccare il codice. A dire se sono giusti sarà l'esperienza degli studenti nella beta
  (deciso il 16/11/2026): vedi §9.

## 1. Punti di una missione e XP (deciso il 16/11/2026)

1.1. Gli **XP** sono i punti skin (blueprint principale §10.3): la somma dei punti massimi degli esercizi
della missione, accreditata solo al superamento e una volta sola.

1.2. **Misura di riferimento** per l'autrice (è un'indicazione, non un vincolo del sistema):

| | Valore |
|---|---:|
| Esercizio in missione | 10 punti (da 5 a 20 se è più corto o più lungo) |
| Missione tipo (5 esercizi) | 50 XP |
| Prova finale del corso | 100 XP |
| Corso tipo (9 missioni + prova finale) | circa 550 XP |

1.3. **Punti skill dalle missioni:** con il fattore 10% una missione da 50 XP dà 5 punti skill; un corso tipo
ne dà circa 55, divisi tra le sue skill. Per salire di livello serve quindi anche la Palestra.

## 2. Livelli delle skill (deciso il 16/11/2026)

2.1. Ogni skill di ogni materia ha un livello, calcolato dai punti dello studente in quella skill e in quella
materia (Palestra + missioni; blueprint principale §10.4-ter).

2.2. Si parte dal **livello 1 con 0 punti**. Nella beta i livelli sono **6**, con la stessa curva per tutte le
skill: ogni passo chiede circa 1,5 volte il precedente.

| Livello | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---:|---:|---:|---:|---:|---:|
| Punti totali necessari | 0 | 20 | 50 | 95 | 165 | 265 |
| Punti dal livello precedente | — | 20 | 30 | 45 | 70 | 100 |

Esempio: con 60 punti di Grammatica in Latino lo studente è al livello 3 e gli mancano 35 punti al livello 4.
I 50 punti del livello 3 coincidono con la soglia del cappello da turista.

2.3. Il livello **non scende mai**: i punti si aggiungono soltanto.

2.4. **Livello massimo.** Al livello 6 lo studente vede "Livello massimo", continua a guadagnare punti (che
servono per gli accessori) e ha tutte le voci aperte. Il tetto si alza quando esistono esercizi più difficili:
dall'Admin si aggiunge il livello 7 con la sua soglia. Chi era al massimo riparte verso il 7 con i punti che ha
già.

2.5. **TODO dopo la beta:** difficoltà che cresce da sola oltre il livello massimo (esercizi generati o resi
più difficili in automatico). Richiede l'AI, che è fuori dall'MVP.

2.6. **Livello di partenza (deciso il 16/11/2026).** Uno studente più grande non deve rifare gli esercizi
facili per arrivare a quelli del suo anno. Il **test di orientamento** (blueprint principale §8.1) assegna un
livello di partenza per ogni skill della materia; chi non fa il test parte dal livello 1.

- Il livello di partenza vale come **punti di partenza** pari alla soglia di quel livello, usati **solo per il
  livello**: la barra riparte da lì e i punti veri si sommano.
- Non sono punti guadagnati: per gli accessori contano solo i punti veri (§6.1).
- Il livello di partenza può solo salire: rifare il test non fa scendere.

Esempio: il test assegna a Grammatica il livello 3 (soglia 50). Lo studente parte al livello 3 con la barra a
zero; dopo 45 punti veri è a 95 e sale al livello 4. Per il cappello gli servono comunque 50 punti veri.

**TODO con il branch dei corsi:** la regola che trasforma il risultato del test in un livello. **Proposta:** il
livello più alto in cui lo studente risponde bene ad almeno il 60% delle domande di quella skill.

## 3. Difficoltà delle voci della Palestra (deciso il 16/11/2026)

3.1. Ogni voce della Palestra ha una **difficoltà da 1 a 6**, sulla stessa scala dei livelli. La sceglie
l'autrice. Le voci già esistenti partono a difficoltà 1.

3.2. Una voce è **aperta** se la sua difficoltà è al massimo "livello della skill + 1", dove il livello è
quello della skill della voce, nella materia della voce.

| Livello della skill | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---|---|---|---|---|---|
| Difficoltà aperte | 1–2 | 1–3 | 1–4 | 1–5 | 1–6 | 1–6 |

3.3. Una voce **bloccata** si vede nell'elenco con titolo, lucchetto e il testo "Si sblocca al livello N di
[skill]" (N = difficoltà − 1), ma non si apre. Esempio: voce di difficoltà 4 → "Si sblocca al livello 3 di
Grammatica".

3.4. Le voci bloccate sono escluse dall'"Obiettivo di oggi" (blueprint principale §7.3).

3.5. Il lucchetto della difficoltà è distinto dal lucchetto premium (blueprint principale §5.6): una voce può
averli entrambi e il frontend li riceve separati.

3.6. **Una sola scala di difficoltà in tutta la piattaforma (deciso il 16/11/2026).** La difficoltà è sempre
quella del livello della skill, skill per skill: non esistono altri contatori di difficoltà che salgono e si
azzerano per conto loro. Esempio: Grammatica al livello 2 e Lessico al livello 1 → lo studente trova voci di
Grammatica fino alla difficoltà 3 e voci di Lessico fino alla 2.

3.7. **Voci più facili.** Salendo di livello le voci più facili restano aperte: lo studente può sempre tornare
a farle. **Da valutare con la beta:** un filtro "Difficoltà" nell'elenco, con le sole difficoltà sbloccate,
per scegliere a che livello allenarsi.

## 3-bis. Programma: la classe di riferimento degli argomenti (deciso il 16/11/2026)

Difficoltà e programma sono due cose diverse. La difficoltà dice quanto è difficile un esercizio (§3); il
programma dice in che anno si studia un argomento. Uno studente di prima molto bravo non deve trovarsi
proposti esercizi su argomenti di quinta.

3b.1. Ogni **argomento** può avere una **classe di riferimento**: l'anno da cui di solito si studia ("dalla
prima", "dalla terza"…). La imposta l'autrice una volta per argomento. Senza classe l'argomento è per tutti.

3b.2. Con la classe dello studente (§6.6) la Palestra si divide in due parti:

- **"Per la tua classe":** argomenti fino al suo anno. Solo questi entrano nell'"Obiettivo di oggi", nei
  consigli, nella sfida giornaliera e nel tris per le vite.
- **"Più avanti nel programma":** argomenti degli anni successivi, con l'etichetta "Di solito si studia in
  [classe]".

3b.3. Gli argomenti degli anni successivi **restano apribili, con un avviso**: nessun lucchetto. Il programma
cambia da scuola a scuola e da docente a docente, e un blocco fermerebbe chi è più avanti della media. Punti e
record valgono normalmente; il lucchetto della difficoltà (§3) resta.

3b.4. Un esercizio "più avanti nel programma" non conta per il tris e non lo azzera se sbagliato.

3b.5. Finché la classe dello studente non esiste (Piano MVP, P.3), o se lo studente non l'ha indicata, la
Palestra mostra tutto in un'unica sezione.

Esempio: studente di prima, Grammatica al livello 4. "Periodo ipotetico" ha classe di riferimento terza: le
sue voci compaiono in "Più avanti nel programma" con l'avviso, anche se la difficoltà sarebbe aperta.

## 4. Evoluzioni del personaggio (deciso il 16/11/2026)

4.1. Nella beta gli stadi sono **3**. La struttura permette di aggiungerne altri.

| Stadio | Condizione | Effetto |
|---|---|---|
| Base | alla registrazione | — |
| Evoluzione I | primo corso completato (blueprint principale §10.4) | base delle vite da 5 a 7 |
| Evoluzione II | **800 XP** e **livello medio delle skill almeno 3** | nuovo aspetto del personaggio |

4.2. **Livello medio:** media semplice dei livelli delle skill di una materia. Contano solo le skill pubblicate
nella materia e con esercizi pubblicati (blueprint principale §10.4-ter). Si calcola per ogni materia dello
studente e vale **la materia migliore**: iniziare una materia nuova non allontana l'evoluzione.

Esempio: Latino con Grammatica 4, Lessico 3, Analisi 2, Traduzione 3, Metrica 2 → media 2,8: non basta.
Quando Analisi sale al livello 3 la media è 3,0: condizione raggiunta. Il Greco appena iniziato (media 1,0)
non conta.

4.3. La media si confronta **senza arrotondare**: 2,8 non vale 3.

4.4. Le due condizioni devono essere vere insieme. Gli stadi si raggiungono in ordine: l'Evoluzione II
richiede l'Evoluzione I.

4.5. Uno stadio raggiunto **non si perde mai**, anche se in seguito cambiano le soglie o le skill della
materia.

4.6. Gli XP sono dello studente: tutti i personaggi mostrano lo stesso stadio (blueprint principale §11.4).

## 5. Personaggi (deciso il 16/11/2026)

5.1. Alla registrazione lo studente sceglie uno dei personaggi iniziali e può cambiarlo liberamente (blueprint
principale §11.4).

5.2. Nella beta gli altri personaggi restano **"Prossimamente"**, senza una regola di sblocco.

5.3. **Roadmap (dopo la beta):** gli altri personaggi **si sbloccheranno**. La regola si decide insieme alle
evoluzioni proprie di ogni personaggio.

5.4. **Personaggi iniziali:** oggi sono **2**, un maschio e una femmina. Giulia sta valutando di portarli a 4
(due maschi e due femmine) e/o di aggiungere figure senza genere. Il numero quindi non è fisso nel codice: è
l'Admin a indicare quali personaggi sono iniziali.

5.5. **Roadmap (dopo la beta):** personalizzazione del personaggio sullo stile di The Sims o Fortnite
(aspetto, abiti, colori). Nella beta l'aspetto non si modifica (blueprint principale §11.4).

## 6. Accessori, bonus e ricompense

6.1. **Accessori della beta (deciso il 16/11/2026):** solo il **cappello da turista**. La struttura permette
di aggiungerne altri dall'Admin. Soglia, riscatto e cariche: blueprint principale §11.1–§11.3.

6.2. **Che cosa fa il cappello (deciso il 16/11/2026).** Lo studente non ricorda l'argomento di un
esercizio: usa il cappello e il cappello gli dice **qual è l'argomento** e **che cosa ripassare** tra le
Risorse della Palestra. Sostituisce l'"aiuto grammaticale" generico del blueprint principale §11.3: l'aiuto
si ricava dall'argomento dell'esercizio e dalla Risorsa collegata a quell'argomento. Cariche, ricarica e
riapertura gratuita nello stesso tentativo restano quelle del §11.3.

- **Dove si usa:** solo nelle missioni. In Palestra le voci sono già raggruppate per argomento.
- **Durante la missione** il cappello apre un riquadro con il nome dell'argomento e l'**"In breve"** della
  Risorsa (testo breve e mini esempio), senza uscire dalla missione; il tempo continua a scorrere.
- **Alla fine del tentativo** compare il collegamento "Ripassa in Palestra" verso la Risorsa intera.
- **Risorsa premium:** l'"In breve" si vede lo stesso, perché è un aiuto guadagnato. La Risorsa intera segue
  le regole free/premium.
- **Esercizio con argomento ma senza Risorsa:** il cappello mostra solo l'argomento.
- **Esercizio senza argomento:** il cappello non è utilizzabile e non consuma cariche.

Esempio: esercizio sulla terza declinazione, 5 cariche. Lo studente usa il cappello: vede "Argomento: terza
declinazione" e l'"In breve" della Risorsa; restano 4 cariche. Se lo riapre nello stesso tentativo, restano 4.

**TODO da valutare dopo la beta:**

- **Catalogo di suggerimenti** scritti apposta per esercizio o per argomento, da valutare anche a partire
  dagli **errori** degli studenti: quali errori sono più frequenti e quale suggerimento li previene (vedi
  `BStudent_Blueprint_Motore_Errori_v0.1.md`). Per deciderlo servono i dati della beta.
- **Aiuti premium:** altri tipi di aiuto riservati a chi ha l'abbonamento. Resta fermo che vite e aiuti non
  si comprano singolarmente (blueprint principale §5.9).

6.3. **Bonus del personaggio (deciso il 16/11/2026): non esistono.** Il riquadro "Bonus attivi" del mockup
("+5% Grammatica") sparisce dalla pagina del personaggio, dalla dashboard e dalla Palestra. I punti sono
uguali per tutti: stesso esercizio, stessa percentuale, stessi punti, qualunque personaggio si usi. L'aiuto
del cappello si legge nella sua scheda tra gli accessori ("Ti ricorda l'argomento · 5 aiuti · si ricarica in
12 ore").

6.4. **Ricompense del corso e della missione (deciso il 16/11/2026).** Nella beta non esistono badge, sfondi,
titoli, ricompensa "misteriosa" né oggetti come l'"Elmo di Bronzo".

- La pagina del corso mostra solo le ricompense vere: gli XP totali del corso e, per il primo corso
  completato, l'Evoluzione I.
- Il traguardo "Ricompensa" alla fine della missione mostra gli XP e i punti skill guadagnati.
- **TODO dopo la beta:** oggetti e premi collegati al mondo virtuale (roadmap).

6.5. **Profilo completato (deciso il 16/11/2026): nessun XP.** Gli XP vengono solo dalle missioni.

6.6. **Scuola, classe e luogo dello studente (deciso il 16/11/2026).**
Servono all'autrice per preparare esercizi adatti: età media, scuole e argomenti più affrontati. Non sono
gamification: appartengono a registrazione e account. Qui resta la decisione; lo sviluppo va pianificato
fuori da questo branch.

- Si chiedono in un passo subito dopo la registrazione ("La tua scuola"), con la frase "Ci serve per
  prepararti esercizi adatti". **Classe obbligatoria, scuola facoltativa** ("Non trovo la mia scuola /
  preferisco non dirlo"): con dei minorenni si chiede come obbligatorio solo l'indispensabile.
- Elenco: scuole medie e superiori.
- La scuola si sceglie da un **elenco di tutte le scuole d'Italia**, importato dai dati aperti del Ministero
  dell'Istruzione (anagrafe delle scuole statali e paritarie). Il luogo si ricava dalla scuola: non si chiede
  a parte.
- La tabella `schools` e il collegamento facoltativo studente → scuola esistono già; mancano l'import, il
  codice ministeriale della scuola, il tipo di scuola e la classe frequentata.
- **TODO legale** (Piano MVP, P.3): dati di minorenni, da indicare nell'informativa con il loro scopo; le
  statistiche per l'autrice sono aggregate.

6.7. **Personaggi della landing pubblica (deciso il 16/11/2026).** La landing promette solo ciò che esiste.

- Personaggi iniziali: "Scegli il tuo quando ti iscrivi".
- Altri personaggi: "Prossimamente". Si sbloccheranno più avanti (§5.3).
- Spariscono i bonus e "Sblocca con 500 XP"; al loro posto "Evolve quando completi il primo corso".
- I testi restano statici nel frontend.

## 7. Sfida giornaliera e Serie (deciso il 16/11/2026)

7.1. **Premio della sfida giornaliera:** **1 vita extra**, una volta al giorno, quando lo studente completa
la sfida "supera 3 oggi" (regole della sfida: blueprint principale §7.3). La vita può superare la base, come
quelle dell'attività dedicata (blueprint principale §12.2), e non riavvia la ricarica. Un doppio invio non
assegna due vite.

Esempio: base 5, saldo 5 → dopo la sfida saldo 6. Base 5, saldo 2 → saldo 3.

7.2. **Serie** (nome mostrato allo studente; "streak" resta il nome tecnico).

- Un giorno **conta** se lo studente supera almeno un esercizio, in Palestra o in missione, con almeno il 60%.
  Il giorno segue l'ora italiana.
- **Un giorno di pausa non la azzera.** La Serie si interrompe dopo due giorni di fila senza studio. Il giorno
  di pausa non aumenta il numero.
- Si mostra anche il **record personale**: una Serie interrotta non cancella il risultato.
- Nessun premio legato alla Serie e nessun messaggio del tipo "stai per perdere la Serie".

| Lun | Mar | Mer | Gio | Ven | Serie a venerdì |
|---|---|---|---|---|---:|
| studia | studia | pausa | studia | studia | 4 |
| studia | studia | pausa | pausa | studia | 1 (record 2) |

## 8. Vite e regole delle skill nelle missioni

### 8.1 Come si ottengono le vite (deciso il 16/11/2026)

Un processo unico, senza meccaniche a parte. L'**attività dedicata** del blueprint principale §12.2 (domanda
con spiegazione libera, autovalutazione, difficoltà che sale e si azzera) **non si sviluppa**: è sostituita
dalle regole qui sotto.

- **3 voci della Palestra superate di fila → 1 vita.** Superata = almeno 60%. Poi il conteggio riparte da zero.
- Un esercizio non superato riporta il conteggio a zero. Uscire senza consegnare non conta e non azzera.
- La stessa voce conta **una volta sola al giorno** (ora italiana): non si accumulano vite ripetendo tre
  esercizi facili. Un secondo tentativo superato nello stesso giorno sulla stessa voce non conta e non azzera.
- Gli esercizi in correzione dalla docente contano quando arriva l'esito.
- La vita può superare la base e non riavvia la ricarica, come già deciso per le vite extra.
- Resta la sfida giornaliera (§7.1): il primo tris del giorno dà quindi 2 vite, quelli dopo 1.
- La ricarica delle 10 ore resta com'è (blueprint principale §12.1).
- Nessun limite al numero di vite ottenute in un giorno, oltre a quello naturale delle voci disponibili.

Esempio: superato, superato, non superato, superato, superato, superato → 1 vita (al sesto esercizio).

### 8.1-bis In Palestra non si perdono vite: la pausa (deciso il 16/11/2026)

- La Palestra **non consuma vite** ed è sempre aperta, anche a zero vite (blueprint principale §7.2, §12.1).
- **Dopo 3 esercizi non superati di fila** la Palestra propone una pausa: "Oggi sembra dura. Rileggi la
  teoria di questo argomento e riprova", con il collegamento alla Risorsa dell'argomento dell'ultimo
  esercizio. È un invito: lo studente può continuare.
- Il costo dell'errore è il conteggio verso la vita che torna a zero (§8.1).
- Scopo: far capire allo studente che, se non è concentrato, conviene fermarsi, senza punire chi ha più
  bisogno di esercitarsi.

### 8.2 Skill negli esercizi delle missioni (deciso il 16/11/2026)

- È l'Admin a indicare le skill di ogni esercizio della missione e quanto conta ciascuna (la somma è 100).
- **Senza skill indicate la missione non si pubblica.** Il messaggio dice quale esercizio va completato.
  Non c'è più l'assegnazione automatica alla skill principale della materia.
- Sostituisce la regola provvisoria del blueprint principale §10.3 ("nessun peso indicato = 100% sulla skill
  predefinita").

Esempio: esercizio da 10 punti, 70% Traduzione e 30% Lessico, risultato 80% → 5,6 punti a Traduzione e 2,4 a
Lessico; con il fattore 10% delle missioni diventano 0,56 e 0,24 punti skill.

**TODO per il branch dei corsi:** il controllo di pubblicazione sta in `MissionsService.exerciseProblems` e
in `IMissionExercisesService`, che questo branch non modifica. Nel form dell'Admin la skill principale della
materia può restare come valore suggerito, che l'autrice conferma.

## 8-bis. Gratuito, abbonamento e crescita del personaggio (deciso il 16/11/2026)

Chiude il punto aperto del blueprint principale §5.6 (completamento del corso ed evoluzione quando ci sono
missioni premium).

### 8b.1 Il corso d'ingresso

- Ogni materia ha un **corso d'ingresso**: è gratuito dall'inizio alla fine, senza missioni premium. L'Admin
  non permette di mettere una missione premium in un corso d'ingresso, né di rendere premium il corso.
- Chiunque può completarlo: l'**Evoluzione I** e le 7 vite sono quindi alla portata di tutti, anche di chi
  non ha l'abbonamento.
- "Corso completato" continua a voler dire **tutte** le missioni superate. Non esiste un completamento
  ridotto per chi non paga.
- Negli altri corsi restano ammesse le missioni premium (blueprint principale §5.6).

### 8b.2 Il muro arriva alla fine del percorso gratuito

- Finito il corso d'ingresso lo studente vede il corso successivo chiuso e l'**Evoluzione II** come traguardo.
- Un corso tipo dà circa 550 XP (§1.2) e l'Evoluzione II ne chiede 800 (§4.1): **con i soli contenuti
  gratuiti non si raggiunge**. È una scelta consapevole: la prima crescita è gratuita per tutti, la seconda
  dipende dai contenuti dell'abbonamento, non da un blocco messo apposta.
- Lo sblocco offerto dall'abbonamento è doppio: nuovi corsi e la crescita successiva del personaggio.

### 8b.3 Recuperare lo studente che non si abbona

**Nella beta:**

1. **La Palestra gratuita resta viva.** Voci gratuite, sfida giornaliera (§7.1) e Serie (§7.2) danno un
   motivo per tornare ogni giorno anche senza corsi nuovi.
2. **"Chiedilo a un genitore".** Al muro lo studente non compra: manda una richiesta all'adulto con un
   riepilogo di ciò che ha fatto (missioni superate, livelli, giorni di studio). Convince il risultato
   mostrato al genitore, non la pressione sul ragazzo.

**TODO dopo la beta:**

3. Novità periodiche anche per chi non paga (una voce gratuita in Palestra, un articolo del Blog).
4. Riepilogo periodico al genitore, con il suo consenso a riceverlo.
5. Prova gratuita dell'abbonamento, attivata dall'adulto.
6. **Uso dell'AI tra i vantaggi dei piani in abbonamento** (per esempio correzione o spiegazioni con l'AI).

### 8b.4 Che cosa non si fa mai

Sono le forme di pressione sui minori da evitare: conti alla rovescia e offerte "che scadono"; personaggio
triste o che regredisce; vite o aiuti in vendita; messaggi di marketing mandati al ragazzo. Paga sempre
l'adulto.

### 8b.5 Momenti che il codice deve far scattare

Sono i punti in cui il sistema deve accorgersi di qualcosa e mostrare il messaggio giusto. Vanno previsti
quando si scrive il codice, non aggiunti dopo.

| Momento | Che cosa scatta |
|---|---|
| Corso d'ingresso completato | Evoluzione I, 7 vite, messaggio di festa; poi il corso successivo e l'Evoluzione II come traguardo |
| Studente senza abbonamento apre un contenuto premium | pagina "Chiedilo a un genitore" con il riepilogo dei suoi risultati |
| Richiesta inviata all'adulto | conferma allo studente; nessun sollecito automatico al ragazzo |
| Studente senza abbonamento che ha finito il gratuito e torna | la home propone la Palestra gratuita e la sfida del giorno, non un invito a pagare a ogni accesso |
| Nuovo stadio raggiunto (§4) | messaggio "Il tuo personaggio si è evoluto" |
| Sfida giornaliera o tris completati (§7.1, §8.1) | messaggio con la vita ottenuta |
| Tre errori di fila in Palestra (§8.1-bis) | invito alla pausa con la teoria da rileggere |
| Cappello riscattabile (§6.1) | avviso "Puoi riscattare il cappello da turista" |

### 8b.6 La parte dell'adulto: da rivedere

Il percorso dell'adulto (ricevere la richiesta, vedere i risultati del ragazzo, pagare, gestire
l'abbonamento, dare i consensi) oggi non è sviluppato abbastanza. Va riprogettato prima dei pagamenti (Piano
MVP, P.2 e P.3): da lì dipendono "Chiedilo a un genitore" e i riepiloghi.

## 9. Numeri da verificare con la beta

A dire se queste scelte sono giuste sarà l'esperienza degli studenti. Durante la beta vanno osservati:

| Numero | Valore di partenza | Segnale che va cambiato |
|---|---|---|
| Soglie dei livelli (§2.2) | 0, 20, 50, 95, 165, 265 | livelli raggiunti troppo presto, oppure nessuno oltre il 3 |
| Sblocco "livello + 1" (§3.2) | +1 | studenti fermi davanti a troppe voci bloccate |
| Evoluzione II (§4.1) | 800 XP e media 3 | nessuno la raggiunge nella beta, oppure arriva insieme alla I |
| Misura delle missioni (§1.2) | 50 XP a missione | corsi con XP molto diversi tra loro |
| Tris per una vita (§8.1) | 3 di fila, una volta al giorno per voce | vite accumulate senza sforzo, oppure studenti fermi a zero |
| Pausa (§8.1-bis) | dopo 3 errori di fila | invito ignorato da tutti, oppure vissuto come un rimprovero |
| Filtro "Difficoltà" (§3.7) | non presente | studenti che cercano le voci più facili senza trovarle |
| Muro alla fine del corso d'ingresso (§8b.2) | Evoluzione II solo con l'abbonamento | studenti gratuiti che smettono subito dopo il primo corso; genitori che non ricevono o ignorano la richiesta |
| Avviso sugli argomenti degli anni dopo (§3b.3) | avviso, nessun blocco | studenti che si perdono in argomenti mai studiati |
| Livello di partenza (§2.6) | dal test di orientamento | studenti partiti troppo in alto che sbagliano tutto, o troppo in basso |

Per poterli cambiare senza perdere nulla: soglie in tabella, stadi raggiunti conservati (§4.5), livelli che
non scendono (§2.3).

## 10. Rappresentazione SQL prevista

Migrazione: `db/bstudent_migration_v0.10.sql`.

Solo aggiunte (colonne con valore predefinito o facoltative, tabelle, viste): il DB è condiviso con il branch
dei corsi. I nomi sono scelte tecniche e si fissano scrivendo la migrazione.

- `practice_exercises`: difficoltà (intero da 1 in su, predefinito 1).
- `courses`: casella "corso d'ingresso", al massimo uno per materia.
- `topics`: classe di riferimento facoltativa (anno di scuola superiore da 1 a 5; vuota = per tutti).
- Livelli di partenza: `student_starting_levels` (studente, materia, skill, livello, data).
- `skill_levels`: resta per skill; le sei righe della curva vengono inserite uguali per ogni skill, così
  un'eccezione per una singola skill resta possibile. Una skill nuova riceve la curva alla creazione.
- Regole degli stadi: tabella `evolution_stage_rules` (numero dello stadio, tipo di regola, soglia di XP,
  livello medio minimo), uguale per tutti i personaggi. `skin_evolutions` resta per le immagini di ogni
  personaggio.
- `skill_credit_entries`: materia (facoltativa nel DB, compilata dal Blocco 5 a ogni accredito).
- Vista `student_skill_totals` (studente, materia, skill, origine), oggi descritta in `StudentSkillTotal.java`
  ma non presente nel DB.
- XP dello studente: registro `student_xp_entries` (studente, missione, XP, data), una riga per missione
  superata. Serve perché gli XP si fissano al momento del superamento: se in seguito cambiano i punti degli
  esercizi, gli XP già guadagnati non cambiano. Il totale è la somma del registro.
- Stadi raggiunti: `student_evolution_stages` (studente, numero dello stadio, data). Lo stadio è dello
  studente, non del personaggio; l'immagine si prende da `skin_evolutions` del personaggio scelto.
- `skins`: casella "personaggio iniziale" e motto facoltativo.
- Vite ottenute: registro `life_awards` (studente, motivo `daily_challenge` o `practice_streak`, giorno,
  evento unico), che impedisce il doppio premio. Serie e conteggio del tris si calcolano dai tentativi
  valutati, senza tabelle.
- `gamification_settings`: esercizi per la sfida (3), esercizi di fila per una vita (3), errori di fila per
  la pausa (3), soglia di superamento già esistente.
- Le tabelle dell'attività dedicata (`life_recovery_activities`, `life_recovery_attempts`) restano nel DB
  inutilizzate: questo branch aggiunge soltanto.
