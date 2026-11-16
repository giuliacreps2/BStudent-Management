/**
 * Test per materia e consigli allo studente (blueprint §8, aggiornato il 9/11/2026).
 *
 * <p>Due test, entrambi nell'MVP e con le risposte salvate:
 * <ul>
 *   <li><b>Orientamento della Palestra</b> (§8.1): ogni opzione consiglia esercizi; al massimo cinque,
 *       prima quelli consigliati da più risposte, poi priorità, poi ID.</li>
 *   <li><b>Livello del corso</b> (§8.2): ogni opzione ha un livello; risultato = il livello più basso
 *       scelto almeno due volte.</li>
 * </ul>
 * Il calcolo avviene qui, non nel frontend. Schema previsto nella v0.9 (§8.4), non ancora applicato.
 *
 * <h2>TODO</h2>
 * <ol>
 *   <li><b>Parte legale, prima della beta (revisione legale).</b> Consigliare contenuti in base alle
 *       risposte e ai risultati è profilazione (GDPR art. 4 n. 4) e gli utenti sono spesso minorenni
 *       (considerando 38 e 71). Da decidere e scrivere:
 *       <ul>
 *         <li>informativa privacy chiara, comprensibile anche da un ragazzo: quali dati usiamo per i
 *             consigli e perché (art. 12-13);</li>
 *         <li>base giuridica dei consigli (esecuzione del servizio di studio o legittimo interesse) e
 *             cosa cambia sotto i 14 anni, dove c'è il consenso del genitore (art. 2-quinquies Codice privacy);</li>
 *         <li>uso dei dati di studio <b>solo</b> per lo studio dentro BStudent: mai marketing, pubblicità
 *             o cessione a terzi;</li>
 *         <li>quanto tempo conservare i risultati dei test (proposta: finché l'account è attivo) e
 *             cancellazione con la chiusura dell'account;</li>
 *         <li>se e come lo studente (o il genitore) può vedere i propri risultati e opporsi ai consigli
 *             personalizzati (art. 15, 21);</li>
 *         <li>nessuna decisione con effetti importanti presa solo dall'algoritmo (art. 22): i consigli
 *             restano suggerimenti, lo studente sceglie sempre liberamente;</li>
 *         <li>distinguere (deciso il 14/11/2026) le <b>statistiche della piattaforma</b>, aggregate e anonime
 *             (esercizi più sbagliati, lezioni più rilette, punti di abbandono), dai <b>consigli al singolo
 *             studente</b>, che sono profilazione. Il registro di studio è descritto nel blueprint §10.5.</li>
 *       </ul></li>
 *   <li><b>Consigli personalizzati per ogni studente, a livelli</b> (effetto "Netflix": righe di
 *       contenuti con un titolo che dice perché sono lì, es. "Perché i verbi ti danno ancora problemi").
 *       <ol>
 *         <li><b>Regole</b> (MVP): ogni riga ha una regola chiara e spiegabile: skill più debole,
 *             esercizi sbagliati, risultati dei test, missione in corso.</li>
 *         <li><b>Punteggio</b> (dopo la beta, con progressi reali): ogni esercizio riceve un punteggio da
 *             più fattori insieme (skill debole, difficoltà vicina al livello, non svolto di recente,
 *             consigliato dal test); gli errori tornano a intervalli crescenti (ripetizione dilazionata).</li>
 *         <li><b>"Studenti come te"</b> (filtraggio collaborativo / modelli che apprendono): solo con
 *             centinaia o migliaia di studenti attivi; prima non ci sono abbastanza dati.</li>
 *       </ol>
 *       Per arrivarci servono i dati raccolti da subito: risultati dei test (§8.3), tentativi e
 *       progressi (Blocco 5). Facoltativo: registrare quali consigli lo studente apre, per misurarne
 *       l'utilità. Ogni livello va rivisto alla luce del punto 1.
 *       Altri punti che useranno lo stesso motore: "Popolare" dei corsi e "Le risorse più usate"
 *       (calcolati dall'uso), "Approfondisci" delle Risorse (tabella dei collegamenti con origine
 *       manual/automatic già prevista, nascosta nell'MVP; blueprint §5.8, §7.4).</li>
 *   <li><b>Orientamento senza esercizi consigliati</b> (§8.1, aperto): messaggio "Sei pronto, scegli tu
 *       cosa allenare" oppure alcuni esercizi generici.</li>
 * </ol>
 */
package giuliacrepaldi.BStudent_Management.services.orientation;
