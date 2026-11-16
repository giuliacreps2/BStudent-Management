package giuliacrepaldi.BStudent_Management.payloads.gamification;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Accessorio, lato Admin. code e slotCode si scelgono alla creazione e non cambiano.
// Aiuto: hintCapacity e hintRechargeHours insieme (es. 5 aiuti, 12 ore), oppure entrambi vuoti = nessun aiuto
public record AccessoryDTO(
        @NotBlank @Size(max = 80) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Usa solo minuscole, numeri e trattini")
        String code,
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 40) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Usa solo minuscole, numeri e trattini")
        String slotCode,
        @NotNull Long skillId,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @DecimalMax("999999.99") BigDecimal requiredPoints,
        boolean available,
        @Positive @Max(50) Integer hintCapacity,
        @Positive @Max(720) Integer hintRechargeHours) {
}
