package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.time.OffsetDateTime;

public record QuestionBankEntryResponseDTO(
        Long questionBankEntryId,
        String internalTitle,
        String title,
        Long topicId,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static QuestionBankEntryResponseDTO from(QuestionBankEntry entry) {
        // Leggere l'id di un proxy LAZY non lo inizializza
        Long topicId = entry.getTopic() == null ? null : entry.getTopic().getTopicId();
        return new QuestionBankEntryResponseDTO(
                entry.getQuestionBankEntryId(),
                entry.getInternalQuestionTitle(),
                entry.getTitle(),
                topicId,
                entry.getStatus(),
                entry.getPublishedAt(),
                entry.getWithdrawnAt(),
                entry.getCreatedAt(),
                entry.getUpdatedAt());
    }
}
