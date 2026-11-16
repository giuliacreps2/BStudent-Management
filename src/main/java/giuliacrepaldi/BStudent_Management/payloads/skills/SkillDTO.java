package giuliacrepaldi.BStudent_Management.payloads.skills;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// code: identificativo stabile in snake_case (es. grammar), fissato alla creazione e non modificabile
public record SkillDTO(
        @NotBlank @Size(max = 60) @Pattern(regexp = "^[a-z][a-z0-9_]*$",
                message = "deve essere in minuscolo snake_case, es. grammar") String code,
        @NotBlank @Size(max = 150) String name,
        String description,
        // Chiavi dell'elenco fisso del frontend; null = aspetto neutro
        @Size(max = 40) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Icona non valida") String icon,
        @Size(max = 40) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Colore non valido") String color) {
}
