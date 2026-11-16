package giuliacrepaldi.BStudent_Management.payloads.gamification;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Soglie di uno stadio: XP minimi e livello medio minimo nella materia migliore
public record EvolutionStageRuleDTO(
        @NotNull @DecimalMin("0.00") @DecimalMax("9999999.99") BigDecimal minXp,
        @NotNull @DecimalMin("1.00") @DecimalMax("30.00") BigDecimal minAverageLevel) {
}
