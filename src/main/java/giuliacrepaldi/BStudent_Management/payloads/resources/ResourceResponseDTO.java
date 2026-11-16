package giuliacrepaldi.BStudent_Management.payloads.resources;

import com.fasterxml.jackson.annotation.JsonRawValue;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.resources.ResourceCategory;

import java.time.OffsetDateTime;

// Vista dell'Admin
public record ResourceResponseDTO(
        Long resourceId,
        Long subjectId,
        ResourceCategory category,
        String slug,
        String title,
        String description,
        String summaryText,
        String coverReference,
        // JSON già validato al salvataggio: inviato così com'è, senza rileggerlo
        @JsonRawValue String content,
        Long topicId,
        AccessTier accessTier,
        boolean isMostUsed,
        String mostUsedIcon,
        boolean isFeatured,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static ResourceResponseDTO from(Resource resource) {
        // Leggere l'id di un proxy LAZY non lo inizializza
        return new ResourceResponseDTO(
                resource.getResourceId(),
                resource.getSubject().getSubjectId(),
                resource.getCategory(),
                resource.getSlug(),
                resource.getTitle(),
                resource.getDescription(),
                resource.getSummaryText(),
                resource.getCoverReference(),
                resource.getContent(),
                resource.getTopic() == null ? null : resource.getTopic().getTopicId(),
                resource.getAccessTier(),
                resource.isMostUsed(),
                resource.getMostUsedIcon(),
                resource.isFeatured(),
                resource.getStatus(),
                resource.getPublishedAt(),
                resource.getWithdrawnAt(),
                resource.getCreatedAt(),
                resource.getUpdatedAt());
    }
}
