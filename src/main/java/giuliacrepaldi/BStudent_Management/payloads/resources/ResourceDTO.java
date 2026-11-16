package giuliacrepaldi.BStudent_Management.payloads.resources;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.resources.ResourceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

// La materia arriva dal path alla creazione; lo stato cambia solo con publish/withdraw
public record ResourceDTO(
        @NotNull ResourceCategory category,
        // Facoltativo: se vuoto si ricava dal titolo; unico nella materia
        @Size(max = 120) String slug,
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 500) String description,
        // "In breve": 2–4 righe
        @Size(max = 600) String summaryText,
        @Size(max = 500) String coverReference,
        // { summaryExample, tool, insights, examples }: forma controllata da ContentBlocks; null = vuoto
        JsonNode content,
        // Argomento della Palestra per "Vai agli esercizi", della stessa materia
        Long topicId,
        @NotNull AccessTier accessTier,
        // "Più usata": serve anche l'icona (chiave dell'elenco fisso del frontend)
        boolean isMostUsed,
        @Size(max = 40) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Icona non valida") String mostUsedIcon,
        boolean isFeatured) {
}
