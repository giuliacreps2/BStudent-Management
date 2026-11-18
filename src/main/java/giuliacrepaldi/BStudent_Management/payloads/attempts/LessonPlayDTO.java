package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonPageResponseDTO;

import java.util.List;

// Lezione della missione: pagine nell'ordine dell'autrice (testo, immagine o video). La lettura non ha tempo
public record LessonPlayDTO(Long lessonId, String title, List<LessonPageResponseDTO> pages) {
}
