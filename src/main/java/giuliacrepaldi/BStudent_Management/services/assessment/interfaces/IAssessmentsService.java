package giuliacrepaldi.BStudent_Management.services.assessment.interfaces;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseViewDTO;

/**
 * Giudizi dei tentativi (blueprint principale §6.1, §6.2, §6.3, §9.3; Blueprint Svolgimento §3, §4, §10).
 * Percentuale da 0 a 100 e feedback; superato con almeno la soglia delle impostazioni dell'Academy (60%),
 * conservata nel giudizio. Il giudizio è definitivo e non si modifica.
 * Scelta multipla e completamento: sempre automatici. Traduzione di versione: sempre la docente.
 * Traduzione di frase: a scelta dell'autrice (alternative accettate oppure docente).
 * I calcoli stanno in utilities.PlayRules (coperti dai test).
 *
 * <h2>TODO — pagina "Consegne da correggere" (Blocco 5, dopo lo svolgimento)</h2>
 * <pre>
 * Page&lt;SubmissionListItemDTO&gt; findPending(Long subjectId, int page, int size);
 *     // consegne in attesa, dalla più vecchia: studente, esercizio, Palestra o missione, data
 * SubmissionDetailDTO getSubmission(long exerciseAttemptId);
 *     // consegna, testo originale, risposta dello studente, soluzione e spiegazione editoriali, rubriche
 *     // come guida (nessun punteggio per criterio)
 * ExerciseAssessment assess(User corrector, long exerciseAttemptId, TeacherAssessmentDTO body);
 *     // percentuale 0-100 e feedback obbligatorio; solo consegne in attesa (annullata o già corretta -&gt;
 *     // ConflictException). Poi gli effetti: Palestra -&gt; IPracticeProgressService.onAssessed;
 *     // missione -&gt; superata oppure tentativo fallito con una vita persa (Svolgimento §1.5)
 * boolean canAssess(User user);
 *     // la regola "chi può correggere" in un punto solo: oggi l'Admin. TODO importante dopo la beta:
 *     // ruolo docente separato, con le sue consegne (Svolgimento §4.2)
 * </pre>
 * Rielaborazione (Svolgimento §8): ultima parte del Blocco 5; si aggancia al tentativo senza modificarlo.
 */
public interface IAssessmentsService {

    // L'esercizio senza soluzione, con le opzioni nell'ordine salvato nel tentativo
    ExerciseViewDTO toView(ExerciseAttempt attempt);

    // Ordine delle opzioni per un tentativo nuovo: mescolate per la scelta multipla, vuoto per gli altri tipi
    java.util.List<Long> newAnswerOrder(long questionVersionId);

    // La domanda del tentativo è a correzione automatica
    boolean isAutomatic(ExerciseAttempt attempt);

    // La risposta ha la forma giusta per il tipo di domanda (un'opzione della domanda, oppure un testo):
    // altrimenti errore. required = false per i salvataggi, dove può essere vuota
    void validateAnswer(ExerciseAttempt attempt, AnswerDTO answer, boolean required);

    // Solo domande a correzione automatica; un giudizio per tentativo (ripetuta: restituisce quello).
    // Chiude il tentativo come valutato
    ExerciseAssessment assessAutomatically(ExerciseAttempt attempt);

    // Che cosa vede lo studente (Svolgimento §3): superato -> risposta giusta, più spiegazione e feedback se
    // esistono; non superato in missione -> feedback e teoria da rileggere, mai la soluzione;
    // non superato in Palestra -> anche la soluzione
    ExerciseOutcomeDTO toOutcome(ExerciseAttempt attempt, ExerciseAssessment assessment);
}
