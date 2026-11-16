package giuliacrepaldi.BStudent_Management.payloads.practice;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

// Lo stato cambia solo con publish/withdraw. L'esercizio del banco si sceglie alla creazione e non cambia
public record PracticeExerciseDTO(
        @NotNull Long questionBankEntryId,
        @NotNull Long skillId,
        @NotNull Long topicId,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @DecimalMax("9999.99") BigDecimal maxPoints,
        // Facoltativo, solo informativo ("~5 min")
        @Positive @Max(240) Integer estimatedMinutes,
        @NotNull AccessTier accessTier,
        // Facoltativa (1 se manca): scala dei livelli della skill. La voce è aperta fino a "livello + 1"
        @Positive @Max(30) Integer difficulty) {
}
