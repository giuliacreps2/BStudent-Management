package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.time.OffsetDateTime;

// Vista dell'Admin: include stato e date editoriali. La posizione nella missione è quella del passo (MissionStepResponseDTO)
public record LessonResponseDTO(
        Long lessonId,
        Long missionId,
        String title,
        Long topicId,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static LessonResponseDTO from(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getLessonId(),
                lesson.getMission().getMissionId(),
                lesson.getLessonTitle(),
                // Leggere l'id di un proxy LAZY non lo inizializza
                lesson.getTopic() == null ? null : lesson.getTopic().getTopicId(),
                lesson.getStatus(),
                lesson.getPublishedAt(),
                lesson.getWithdrawnAt(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt());
    }
}
