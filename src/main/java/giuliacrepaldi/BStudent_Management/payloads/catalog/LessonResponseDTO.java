package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.time.OffsetDateTime;

// Vista dell'Admin: include stato e date editoriali
public record LessonResponseDTO(
        Long lessonId,
        Long missionId,
        String title,
        int sortOrder,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt) {

    public static LessonResponseDTO from(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getLessonId(),
                lesson.getMission().getMissionId(),
                lesson.getLessonTitle(),
                lesson.getLessonSortOrder(),
                lesson.getStatus(),
                lesson.getPublishedAt(),
                lesson.getWithdrawnAt());
    }
}
