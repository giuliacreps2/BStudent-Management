package giuliacrepaldi.BStudent_Management.payloads.exercises;

import java.time.OffsetDateTime;

// Riepilogo delle versioni di un esercizio (QuestionVersionsRepository.findVersionInfo).
// publishedVersionNumber e publishedVersionSince sono null se nessuna versione è pubblicata
public record QuestionBankEntryVersionInfo(
        Long questionBankEntryId,
        OffsetDateTime firstVersionAt,
        Integer publishedVersionNumber,
        OffsetDateTime publishedVersionSince) {
}
