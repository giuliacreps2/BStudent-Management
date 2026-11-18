package giuliacrepaldi.BStudent_Management.payloads.attempts;

import java.math.BigDecimal;

// Riscontro dopo il salvataggio della correzione: effect dice in parole semplici che cosa succede allo studente
public record SubmissionAssessedDTO(long exerciseAttemptId, BigDecimal percentage, BigDecimal passingThreshold,
                                    boolean passed, String feedback, String effect) {
}
