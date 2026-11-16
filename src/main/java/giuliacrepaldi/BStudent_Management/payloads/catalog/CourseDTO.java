package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

// La materia arriva dal path alla creazione; lo stato cambia solo con publish/withdraw
public record CourseDTO(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String shortDescription,
        @NotBlank String extendedDescription,
        // Facoltativa: senza copertina la UI usa un fallback
        @Size(max = 500) String coverReference,
        @NotNull AccessTier accessTier,
        // Facoltativo: se vuoto si ricava dal titolo
        @Size(max = 120) String slug,
        @Size(max = 300) String tagline,
        // Facoltativo in bozza, obbligatorio per pubblicare
        CourseLevel level,
        // null = non in evidenza
        OffsetDateTime featuredUntil,
        @Size(max = 60) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Tema del mondo non valido")
        String worldTheme,
        @Size(max = 500) String quote,
        @Size(max = 150) String quoteAuthor,
        // Facoltativo (false se manca): corso d'ingresso della materia, gratuito dall'inizio alla fine
        Boolean entryCourse) {
}
