package giuliacrepaldi.BStudent_Management.payloads.gamification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Uno stadio del personaggio, lato Admin: nome mostrato e immagine (identificativo Cloudinary)
public record CharacterStageDTO(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 500) String imageReference) {
}
