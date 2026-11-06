package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;

import java.time.OffsetDateTime;

// Elenco delle versioni senza contenuto; il dettaglio è in QuestionVersionResponseDTO
public record QuestionVersionSummaryDTO(
        Long questionVersionId,
        Long questionBankEntryId,
        int versionNumber,
        boolean published,
        OffsetDateTime publishedAt) {

    public static QuestionVersionSummaryDTO from(QuestionVersion version) {
        return new QuestionVersionSummaryDTO(
                version.getQuestionVersionId(),
                version.getQuestionBankEntry().getQuestionBankEntryId(),
                version.getQuestionVersionNumber(),
                version.isPublished(),
                version.getPublishedAt());
    }
}
