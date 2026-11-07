package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.time.OffsetDateTime;

// Riga dell'elenco esercizi dell'Admin: dati dell'esercizio più il riepilogo delle sue versioni
public record QuestionBankEntryListItemDTO(
        Long questionBankEntryId,
        String internalTitle,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt,
        OffsetDateTime createdAt,
        // Comprende le modifiche alle versioni
        OffsetDateTime updatedAt,
        // null se l'esercizio non ha ancora versioni
        OffsetDateTime firstVersionAt,
        // null se nessuna versione è pubblicata
        Integer publishedVersionNumber,
        OffsetDateTime publishedVersionSince) {

    public static QuestionBankEntryListItemDTO of(QuestionBankEntry entry, QuestionBankEntryVersionInfo info) {
        return new QuestionBankEntryListItemDTO(
                entry.getQuestionBankEntryId(),
                entry.getInternalQuestionTitle(),
                entry.getStatus(),
                entry.getPublishedAt(),
                entry.getWithdrawnAt(),
                entry.getCreatedAt(),
                entry.getUpdatedAt(),
                info == null ? null : info.firstVersionAt(),
                info == null ? null : info.publishedVersionNumber(),
                info == null ? null : info.publishedVersionSince());
    }
}
