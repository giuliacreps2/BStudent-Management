package giuliacrepaldi.BStudent_Management.services.attempts.interfaces;

/**
 * Tentativi della Palestra (blueprint principale §7.2, §10.1; Blueprint Svolgimento §3, §6, §7).
 * Avvio senza vite; salva, riprendi, esci senza salvare, consegna. Una sola bozza o consegna aperta per la
 * stessa voce. Il tempo è facoltativo e non cambia la valutazione. In Palestra non si perdono mai vite.
 *
 * <h2>TODO — metodi da implementare (Blocco 5)</h2>
 * <pre>
 * PracticeAttemptDTO start(long studentId, long practiceExerciseId);
 *     // voce visibile (StudentVisibility); lucchetto della difficoltà chiuso -&gt; ConflictException con
 *     // "Si sblocca al livello N di [skill]"; premium senza accesso -&gt; ForbiddenException.
 *     // Bozza già aperta -&gt; restituisce quella (riprendi). Consegna in correzione -&gt; ConflictException
 *     // "È in correzione: puoi fare altri esercizi". Tentativo nuovo: versione pubblicata dell'esercizio,
 *     // opzioni mescolate e salvate nella configurazione del tentativo
 * PracticeAttemptDTO getAttempt(long studentId, long exerciseAttemptId);
 *     // domanda senza soluzione finché non c'è l'esito; dopo l'esito anche ExerciseOutcomeDTO
 * PracticeAttemptDTO saveDraft(long studentId, long exerciseAttemptId, AnswerDTO body);
 *     // "Salva esercizio": conserva la risposta; stato salvato
 * void leave(long studentId, long exerciseAttemptId);
 *     // "Esce senza salvare": tentativo abbandonato, nessun esito; il prossimo avvio parte da zero
 * ExerciseOutcomeDTO submit(long studentId, long exerciseAttemptId, AnswerDTO body);
 *     // correzione automatica: esito subito (IAssessmentsService.assessAutomatically), poi record, punti e
 *     // premi (IPracticeProgressService.onAssessed). Correzione della docente: "in correzione", risposta
 *     // bloccata, nessun esito. Doppio invio: stesso risultato, nessun doppio effetto
 * PracticeAttemptDTO cancelSubmission(long studentId, long exerciseAttemptId);
 *     // "Annulla invio": solo se in correzione e senza esito; torna salvato con la risposta modificabile.
 *     // Esito già dato -&gt; ConflictException "La docente ha già corretto: l'esito è definitivo"
 * </pre>
 * Esercizio ritirato dal banco con un tentativo aperto (§6): il tentativo si chiude senza esito alla prima
 * lettura; una consegna già in correzione resta valida.
 * DTO da creare: PracticeAttemptDTO (stato, domanda, risposta salvata, esito), ExerciseViewDTO (tipo,
 * consegna, testo originale, opzioni nell'ordine del tentativo: mai la soluzione), AnswerDTO (opzione scelta
 * oppure testo), ExerciseOutcomeDTO (percentuale, superato, feedback, risposta giusta e spiegazione secondo
 * il §3, premi ottenuti).
 * Controller: /student/practice/attempts/**.
 */
public interface IPracticeAttemptsService {
}
