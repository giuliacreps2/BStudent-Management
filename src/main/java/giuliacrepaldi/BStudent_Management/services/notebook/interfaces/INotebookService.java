package giuliacrepaldi.BStudent_Management.services.notebook.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.notebook.LessonNoteDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.LessonNoteResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.NotebookLessonDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.NotebookOverviewDTO;

/**
 * Quaderno dello studente (blueprint §14; "Taccuino" nel blueprint, "Quaderno" nell'interfaccia).
 * Sono gli appunti, non il libro: lezioni delle missioni (anche video) con la nota personale e articoli
 * del Blog preferiti. Le Risorse salvate sono solo segnalibri, separati dagli appunti (§7.4).
 * Preferiti del Blog: con il Blog (v0.13), non ancora.
 * Controller: /student/notebook/**.
 */
public interface INotebookService {

    // "Ho finito di leggere" nella missione (Blocco 5, MissionRunsService): la lezione entra nel Quaderno.
    // Già presente: nessun errore e nessun duplicato. Lo studente non può toglierla
    void acquireLesson(long studentId, long lessonId);

    // Linguette per materia con le lezioni, colore della copertina e segnalibri.
    // Lezione ritirata: resta solo se ha una nota ("lezione non più disponibile"); senza nota sparisce.
    // Premium scaduto: titolo, anteprima e nota, lezione con lucchetto
    NotebookOverviewDTO getOverview(long studentId);

    // Lezione in sola lettura con la nota; 404 se non è nel Quaderno (o è ritirata e senza nota)
    NotebookLessonDTO getLesson(long studentId, long lessonId);

    // Salvataggio automatico: version è quella letta; se nel frattempo è cambiata (due schede) -> 409.
    // Testo vuoto = cancellare la nota (restituisce null)
    LessonNoteResponseDTO saveNote(long studentId, long lessonId, LessonNoteDTO body);

    void deleteNote(long studentId, long lessonId);

    // Chiave della tavolozza del frontend, null = predefinito
    void setCoverColor(long studentId, String color);

    // Risorsa visibile allo studente; idempotente
    void addBookmark(long studentId, long resourceId);

    void removeBookmark(long studentId, long resourceId);
}
