package giuliacrepaldi.BStudent_Management.payloads.gamification;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

// Punti necessari per ogni livello, in ordine: il primo è 0 (livello 1).
// applyToAllSkills = usa la stessa curva per tutte le skill
public record SkillLevelsDTO(
        @NotEmpty @Size(max = 30) List<@NotNull @DecimalMin("0.00") BigDecimal> minPoints,
        boolean applyToAllSkills) {
}
