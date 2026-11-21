package giuliacrepaldi.BStudent_Management.payloads.notebook;

import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonPageResponseDTO;

import java.util.List;

// Lezione aperta dal Quaderno, in sola lettura, con la nota. pages è vuoto se la lezione non è più disponibile
// o se è premium e l'abbonamento è scaduto (item.available / item.premiumLocked dicono quale dei due)
public record NotebookLessonDTO(NotebookItemDTO item, List<LessonPageResponseDTO> pages, LessonNoteResponseDTO note) {
}
