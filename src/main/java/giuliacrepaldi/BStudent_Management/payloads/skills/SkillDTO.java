package giuliacrepaldi.BStudent_Management.payloads.skills;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// code: identificativo stabile in snake_case (es. grammar), fissato alla creazione e non modificabile
public record SkillDTO(
        @NotBlank @Size(max = 60) @Pattern(regexp = "^[a-z][a-z0-9_]*$",
                message = "deve essere in minuscolo snake_case, es. grammar") String code,
        @NotBlank @Size(max = 150) String name,
        String description) {
}
