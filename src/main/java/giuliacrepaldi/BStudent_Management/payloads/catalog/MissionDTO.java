package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

// Il corso arriva dal path alla creazione; ordine e stato cambiano solo con reorder e publish/withdraw
public record MissionDTO(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String shortDescription,
        // null = eredita dal corso
        AccessTier accessTier,
        // Facoltativo: se vuoto si ricava dal titolo; unico nel corso
        @Size(max = 120) String slug,
        @Size(max = 300) String tagline,
        @Size(max = 500) String coverReference,
        // Prova finale: al massimo una per corso
        boolean isFinal,
        // Video del briefing: entrambi o nessuno; youtube per i contenuti free, vimeo per i premium (§5.1)
        @Pattern(regexp = "youtube|vimeo", message = "Piattaforma video non valida: YouTube o Vimeo")
        String briefingVideoProvider,
        @Size(max = 200) String briefingVideoIdentifier,
        // Battute della guida (fino a 5) e "Cosa imparerai" (fino a 5); null = nessuna
        @Size(max = 5) List<@NotBlank @Size(max = 500) String> briefingLines,
        @Size(max = 5) List<@NotBlank @Size(max = 200) String> learningGoals) {
}
