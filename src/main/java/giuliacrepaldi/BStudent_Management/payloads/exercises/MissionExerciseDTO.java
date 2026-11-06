package giuliacrepaldi.BStudent_Management.payloads.exercises;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

// Collegamento: questionBankEntryId obbligatorio. Modifica: non si cambia esercizio (si scollega e ricollega).
// Tempo e punti null -> default di academy_settings alla creazione, invariati alla modifica
public record MissionExerciseDTO(
        Long questionBankEntryId,
        @Positive Long allowedDurationMs,
        @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal maxPoints) {
}
