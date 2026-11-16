package giuliacrepaldi.BStudent_Management.payloads.practice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// La materia arriva dal path alla creazione e non cambia
public record TopicDTO(
        @NotBlank @Size(max = 150) String name,
        // Identificativo Cloudinary, facoltativo: senza, la Palestra usa l'immagine della skill
        @Size(max = 500) String imageReference) {
}
