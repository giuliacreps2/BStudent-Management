package giuliacrepaldi.BStudent_Management.payloads.attempts;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Giudizio della docente: percentuale da 0 a 100 e un commento per lo studente (Blueprint Svolgimento §4.1)
public record TeacherAssessmentDTO(
        @NotNull(message = "Indica la percentuale, da 0 a 100")
        @DecimalMin(value = "0", message = "La percentuale va da 0 a 100")
        @DecimalMax(value = "100", message = "La percentuale va da 0 a 100")
        @Digits(integer = 3, fraction = 2, message = "La percentuale può avere al massimo due decimali")
        BigDecimal percentage,
        @NotBlank(message = "Scrivi un commento per lo studente: lo legge insieme al risultato")
        @Size(max = 5000, message = "Il commento può avere al massimo 5000 caratteri")
        String feedback) {
}
