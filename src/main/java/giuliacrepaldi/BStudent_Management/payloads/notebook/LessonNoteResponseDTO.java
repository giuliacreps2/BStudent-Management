package giuliacrepaldi.BStudent_Management.payloads.notebook;

import giuliacrepaldi.BStudent_Management.entities.notebook.LessonNote;

import java.time.OffsetDateTime;

// Nota della lezione: version va rimandata al prossimo salvataggio
public record LessonNoteResponseDTO(String text, long version, OffsetDateTime updatedAt) {

    public static LessonNoteResponseDTO from(LessonNote note) {
        return new LessonNoteResponseDTO(note.getNoteText(), note.getVersion(), note.getUpdatedAt());
    }
}
