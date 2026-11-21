package giuliacrepaldi.BStudent_Management.payloads.notebook;

import java.time.OffsetDateTime;

// Una lezione nel Quaderno (blueprint §14). kind: lesson | video (lezione fatta solo di video).
// preview = primo paragrafo, null se la lezione non è disponibile o è bloccata.
// available = false: lezione ritirata, resta solo la nota con "lezione non più disponibile".
// premiumLocked = abbonamento scaduto: titolo, anteprima e nota sì, la lezione completa no.
// isNew = entrata e mai riaperta (dagli eventi di studio); hasNote = c'è la nota dello studente
public record NotebookItemDTO(Long lessonId, String kind, String title, Long topicId, String topicName, String preview,
                              OffsetDateTime acquiredAt, String courseSlug, String courseTitle, String missionSlug,
                              String missionTitle, boolean available, boolean premiumLocked, boolean isNew,
                              boolean hasNote) {
}
