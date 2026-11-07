package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;

import java.time.OffsetDateTime;

// Elenco delle versioni senza contenuto; il dettaglio è in QuestionVersionResponseDTO
public record QuestionVersionSummaryDTO(
        Long questionVersionId,
        Long questionBankEntryId,
        int versionNumber,
        boolean published,
        OffsetDateTime publishedAt,
        // Fine dell'uso come versione pubblicata: null se in uso o mai pubblicata
        OffsetDateTime replacedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static QuestionVersionSummaryDTO from(QuestionVersion version) {
        return new QuestionVersionSummaryDTO(
                version.getQuestionVersionId(),
                version.getQuestionBankEntry().getQuestionBankEntryId(),
                version.getQuestionVersionNumber(),
                version.isPublished(),
                version.getPublishedAt(),
                version.getReplacedAt(),
                version.getCreatedAt(),
                version.getUpdatedAt());
    }
}
