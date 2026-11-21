package giuliacrepaldi.BStudent_Management.payloads.notebook;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// Salvataggio automatico della nota. version = la versione letta dallo studente (null per una nota nuova):
// se nel frattempo è cambiata (due schede aperte) il salvataggio viene rifiutato. Testo vuoto = cancellare
public record LessonNoteDTO(@Size(max = 20000, message = "La nota può avere al massimo 20000 caratteri") String text,
                            @Positive Long version) {
}
