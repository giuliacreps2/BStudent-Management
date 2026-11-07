package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.time.OffsetDateTime;

// Vista dell'Admin: accessTier null = eredita dal corso
public record MissionResponseDTO(
        Long missionId,
        Long courseId,
        String title,
        String shortDescription,
        int sortOrder,
        AccessTier accessTier,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static MissionResponseDTO from(Mission mission) {
        return new MissionResponseDTO(
                mission.getMissionId(),
                mission.getCourse().getCourseId(),
                mission.getMissionTitle(),
                mission.getMissionDescription(),
                mission.getMissionSortOrder(),
                mission.getAccessTier(),
                mission.getStatus(),
                mission.getPublishedAt(),
                mission.getWithdrawnAt(),
                mission.getCreatedAt(),
                mission.getUpdatedAt());
    }
}
