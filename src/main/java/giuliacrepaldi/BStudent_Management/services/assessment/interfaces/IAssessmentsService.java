package giuliacrepaldi.BStudent_Management.services.assessment.interfaces;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseViewDTO;

import java.math.BigDecimal;

/**
 * Giudizi dei tentativi (blueprint principale §6.1, §6.2, §6.3, §9.3; Blueprint Svolgimento §3, §4, §10).
 * Percentuale da 0 a 100 e feedback; superato con almeno la soglia delle impostazioni dell'Academy (60%),
 * conservata nel giudizio. Il giudizio è definitivo e non si modifica.
 * Scelta multipla e completamento: sempre automatici. Traduzione di versione: sempre la docente.
 * Traduzione di frase: a scelta dell'autrice (alternative accettate oppure docente).
 * I calcoli stanno in utilities.PlayRules (coperti dai test).
 *
 * La pagina "Consegne da correggere" sta in ISubmissionsService, che usa assessByTeacher.
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

    // Giudizio della docente su una consegna in attesa: superato con almeno la soglia dell'Academy.
    // Chiude il tentativo come valutato; gli effetti (punti, missione) li applica chi chiama
    ExerciseAssessment assessByTeacher(ExerciseAttempt attempt, User teacher, BigDecimal percentage, String feedback);

    // Soglia di superamento delle impostazioni dell'Academy (60%)
    BigDecimal passingThreshold();

    // Che cosa vede lo studente (Svolgimento §3): superato -> risposta giusta, più spiegazione e feedback se
    // esistono; non superato in missione -> feedback e teoria da rileggere, mai la soluzione;
    // non superato in Palestra -> anche la soluzione
    ExerciseOutcomeDTO toOutcome(ExerciseAttempt attempt, ExerciseAssessment assessment);
}
