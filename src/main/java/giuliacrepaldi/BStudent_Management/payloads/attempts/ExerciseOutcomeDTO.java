package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeRewardsDTO;

import java.math.BigDecimal;
import java.util.List;

// Esito di un esercizio e che cosa vede lo studente (Blueprint Svolgimento §3).
// correctAnswers c'è se l'esercizio è superato, oppure sempre in Palestra; è vuoto dopo un errore in missione.
// generalExplanation solo se l'autrice l'ha scritta e la soluzione è visibile.
// reviewTopicName / reviewResourceSlug: la teoria da rileggere dopo un errore, se l'esercizio ha un argomento.
// pointsEarned = punti skill accreditati adesso; rewards = vite del tris e della sfida (solo Palestra)
public record ExerciseOutcomeDTO(BigDecimal percentage, BigDecimal passingThreshold, boolean passed, String feedback,
                                 List<String> correctAnswers, String generalExplanation, String reviewTopicName,
                                 String reviewSubjectSlug, String reviewResourceSlug, BigDecimal pointsEarned,
                                 PracticeRewardsDTO rewards) {

    public ExerciseOutcomeDTO withRewards(BigDecimal earned, PracticeRewardsDTO practiceRewards) {
        return new ExerciseOutcomeDTO(percentage, passingThreshold, passed, feedback, correctAnswers, generalExplanation,
                reviewTopicName, reviewSubjectSlug, reviewResourceSlug, earned, practiceRewards);
    }
}
