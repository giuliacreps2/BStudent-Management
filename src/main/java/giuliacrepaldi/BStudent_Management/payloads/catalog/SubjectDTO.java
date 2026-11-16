package giuliacrepaldi.BStudent_Management.payloads.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Lo stato cambia solo con publish/unpublish
public record SubjectDTO(
        @NotBlank @Size(max = 150) String name,
        // Facoltativo: se vuoto si ricava dal nome
        @Size(max = 120) String slug,
        @Size(max = 300) String tagline,
        @Size(max = 500) String imageReference,
        // Guida del briefing delle missioni (facoltativa)
        @Size(max = 100) String guideName,
        @Size(max = 500) String guideImageReference,
        // Fallback dei pesi skill degli esercizi di missione; null = pesi sempre espliciti
        Long defaultSkillId) {
}
