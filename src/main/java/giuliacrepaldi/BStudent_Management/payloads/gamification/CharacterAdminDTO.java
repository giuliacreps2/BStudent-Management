package giuliacrepaldi.BStudent_Management.payloads.gamification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Personaggio, lato Admin. Il code si sceglie alla creazione e non cambia: il frontend lo può usare.
// starter = personaggio iniziale, che lo studente può scegliere; gli altri compaiono come "Prossimamente"
public record CharacterAdminDTO(
        @NotBlank @Size(max = 80) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Usa solo minuscole, numeri e trattini")
        String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @Size(max = 300) String motto,
        boolean starter) {
}
