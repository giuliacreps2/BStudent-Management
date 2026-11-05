package giuliacrepaldi.BStudent_Management.payloads.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubjectDTO(
        @NotBlank @Size(max = 150) String name,
        // Fallback dei pesi skill degli esercizi di missione; null = pesi sempre espliciti
        Long defaultSkillId) {
}
