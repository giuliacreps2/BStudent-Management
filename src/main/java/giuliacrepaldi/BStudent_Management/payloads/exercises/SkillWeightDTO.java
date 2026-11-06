package giuliacrepaldi.BStudent_Management.payloads.exercises;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Percentuale del risultato dell'esercizio attribuita alla skill
public record SkillWeightDTO(
        @NotNull Long skillId,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @DecimalMax("100.00")
        @Digits(integer = 3, fraction = 2) BigDecimal weight) {
}
