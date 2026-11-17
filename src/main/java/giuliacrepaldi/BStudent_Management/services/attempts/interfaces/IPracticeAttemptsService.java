package giuliacrepaldi.BStudent_Management.services.attempts.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.PracticeAttemptDTO;

/**
 * Tentativi della Palestra (blueprint principale §7.2, §10.1; Blueprint Svolgimento §3, §6, §7).
 * Avvio senza vite; salva, riprendi, esci senza salvare, consegna. Una sola bozza o consegna aperta per la
 * stessa voce. Il tempo è facoltativo e non cambia la valutazione. In Palestra non si perdono mai vite.
 *
 * <p>Esercizio ritirato dal banco con un tentativo aperto (§6): il tentativo si chiude senza esito alla
 * prima lettura; una consegna già in correzione resta valida.
 */
public interface IPracticeAttemptsService {

    // Voce visibile; lucchetto della difficoltà chiuso -> errore con "Si sblocca al livello N di [skill]";
    // premium senza accesso -> vietato. Bozza già aperta -> restituisce quella (riprendi).
    // Consegna in correzione -> errore. Tentativo nuovo: versione pubblicata e opzioni mescolate
    PracticeAttemptDTO start(long studentId, long practiceExerciseId);

    PracticeAttemptDTO getAttempt(long studentId, long exerciseAttemptId);

    // "Salva esercizio": conserva la risposta; stato salvato
    PracticeAttemptDTO saveDraft(long studentId, long exerciseAttemptId, AnswerDTO body);

    // "Esce senza salvare": tentativo abbandonato, nessun esito; il prossimo avvio parte da zero
    void leave(long studentId, long exerciseAttemptId);

    // Correzione automatica: esito subito, poi record, punti e premi. Correzione della docente: "in correzione",
    // risposta bloccata. Doppio invio: stesso risultato, nessun doppio effetto
    PracticeAttemptDTO submit(long studentId, long exerciseAttemptId, AnswerDTO body);

    // "Annulla invio": solo se in correzione e senza esito; torna salvato con la risposta modificabile
    PracticeAttemptDTO cancelSubmission(long studentId, long exerciseAttemptId);
}
