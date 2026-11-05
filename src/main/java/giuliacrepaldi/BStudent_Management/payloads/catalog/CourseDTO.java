package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// La materia arriva dal path alla creazione; lo stato cambia solo con publish/withdraw
public record CourseDTO(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String shortDescription,
        @NotBlank String extendedDescription,
        // Facoltativa: senza copertina la UI usa un fallback
        @Size(max = 500) String coverReference,
        @NotNull AccessTier accessTier) {
}
