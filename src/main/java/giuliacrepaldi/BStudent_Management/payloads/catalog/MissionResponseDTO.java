package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.time.OffsetDateTime;
import java.util.List;

// Vista dell'Admin: accessTier null = eredita dal corso
public record MissionResponseDTO(
        Long missionId,
        Long courseId,
        String title,
        String shortDescription,
        int sortOrder,
        AccessTier accessTier,
        String slug,
        String tagline,
        String coverReference,
        boolean isFinal,
        String briefingVideoProvider,
        String briefingVideoIdentifier,
        List<String> briefingLines,
        List<String> learningGoals,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    // Legge le due liste LAZY: funziona nel controller perché open-in-view è attivo (default di Spring)
    public static MissionResponseDTO from(Mission mission) {
        return new MissionResponseDTO(
                mission.getMissionId(),
                mission.getCourse().getCourseId(),
                mission.getMissionTitle(),
                mission.getMissionDescription(),
                mission.getMissionSortOrder(),
                mission.getAccessTier(),
                mission.getSlug(),
                mission.getTagline(),
                mission.getCoverReference(),
                mission.isFinal(),
                mission.getBriefingVideoProvider(),
                mission.getBriefingVideoIdentifier(),
                List.copyOf(mission.getBriefingLines()),
                List.copyOf(mission.getLearningGoals()),
                mission.getStatus(),
                mission.getPublishedAt(),
                mission.getWithdrawnAt(),
                mission.getCreatedAt(),
                mission.getUpdatedAt());
    }
}
