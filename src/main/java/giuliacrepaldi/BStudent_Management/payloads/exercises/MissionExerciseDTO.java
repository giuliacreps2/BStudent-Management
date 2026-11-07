package giuliacrepaldi.BStudent_Management.payloads.exercises;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

// Collegamento: questionBankEntryId obbligatorio. Modifica: non si cambia esercizio (si scollega e ricollega).
// Tempo e punti null -> default di academy_settings alla creazione, invariati alla modifica.
// skills solo alla creazione, stesse regole di PUT .../skills (null = nessuna indicata); poi si cambiano da lì
public record MissionExerciseDTO(
        Long questionBankEntryId,
        @Positive Long allowedDurationMs,
        @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal maxPoints,
        @Size(max = 10) List<@Valid @NotNull SkillWeightDTO> skills) {
}
