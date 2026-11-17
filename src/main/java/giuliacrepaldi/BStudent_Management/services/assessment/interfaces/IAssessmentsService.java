package giuliacrepaldi.BStudent_Management.services.assessment.interfaces;

/**
 * Giudizi dei tentativi (blueprint principale §6.1, §6.2, §6.3, §9.3; Blueprint Svolgimento §3, §4).
 * Percentuale da 0 a 100 e feedback; superato con almeno la soglia delle impostazioni dell'Academy (60%),
 * conservata nel giudizio. Il giudizio è definitivo e non si modifica.
 * Scelta multipla e completamento: sempre automatici. Traduzione di versione: sempre la docente.
 * Traduzione di frase: a scelta dell'autrice (alternative accettate oppure docente).
 *
 * <h2>TODO — metodi da implementare (Blocco 5)</h2>
 * <pre>
 * // Calcolo puro, con i test (utilities.ExerciseGrader):
 * static BigDecimal grade(KindQuestion kind, List&lt;QuestionAnswer&gt; answers, AnswerDTO response);
 *     // scelta multipla: 100 se l'opzione scelta è quella giusta, altrimenti 0.
 *     // completamento e traduzione di frase con alternative: 100 se il testo coincide con una delle
 *     // risposte accettate, senza differenze di maiuscole e di spazi esterni; altrimenti 0
 *
 * ExerciseAssessment assessAutomatically(ExerciseAttempt attempt);
 *     // solo domande a correzione automatica; un giudizio per tentativo (ripetuta: restituisce quello)
 * ExerciseOutcomeDTO toOutcome(ExerciseAttempt attempt, ExerciseAssessment assessment);
 *     // che cosa vede lo studente (§3): superato -&gt; risposta giusta, più spiegazione e feedback se
 *     // esistono; non superato in missione -&gt; feedback e invito a rileggere la teoria, mai la soluzione;
 *     // non superato in Palestra -&gt; anche la soluzione
 *
 * // Admin: "Consegne da correggere" (§4)
 * Page&lt;SubmissionListItemDTO&gt; findPending(Long subjectId, int page, int size);
 *     // consegne in attesa, dalla più vecchia: studente, esercizio, Palestra o missione, data
 * SubmissionDetailDTO getSubmission(long exerciseAttemptId);
 *     // consegna, testo originale, risposta dello studente, soluzione e spiegazione editoriali, rubriche
 *     // come guida (nessun punteggio per criterio)
 * ExerciseAssessment assess(User corrector, long exerciseAttemptId, TeacherAssessmentDTO body);
 *     // percentuale 0-100 e feedback obbligatorio; solo consegne in attesa (annullata o già corretta -&gt;
 *     // ConflictException). Poi gli effetti: Palestra -&gt; IPracticeProgressService.onAssessed;
 *     // missione -&gt; superata oppure tentativo fallito con una vita persa (§1.5)
 * boolean canAssess(User user);
 *     // la regola "chi può correggere" in un punto solo: oggi l'Admin. TODO importante dopo la beta:
 *     // ruolo docente separato, con le sue consegne (§4.2)
 * </pre>
 * Rielaborazione (§8): fuori dal Blocco 5; si potrà agganciare al tentativo senza modificarlo.
 * DTO da creare: SubmissionListItemDTO, SubmissionDetailDTO, TeacherAssessmentDTO.
 * Controller: /admin/academy/submissions/**.
 */
public interface IAssessmentsService {
}
