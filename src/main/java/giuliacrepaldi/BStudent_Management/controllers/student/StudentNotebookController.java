package giuliacrepaldi.BStudent_Management.controllers.student;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.notebook.CoverColorDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.LessonNoteDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.LessonNoteResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.NotebookLessonDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.NotebookOverviewDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.notebook.interfaces.INotebookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Quaderno dello studente (blueprint §14): lo studente si ricava dal token
@RestController
@RequestMapping("/student/notebook")
public class StudentNotebookController {

    private final CurrentStudent currentStudent;
    private final INotebookService notebookService;

    public StudentNotebookController(CurrentStudent currentStudent, INotebookService notebookService) {
        this.currentStudent = currentStudent;
        this.notebookService = notebookService;
    }

    @GetMapping
    public NotebookOverviewDTO overview(@AuthenticationPrincipal User user) {
        return notebookService.getOverview(currentStudent.idOf(user));
    }

    @GetMapping("/lessons/{lessonId}")
    public NotebookLessonDTO lesson(@AuthenticationPrincipal User user, @PathVariable long lessonId) {
        return notebookService.getLesson(currentStudent.idOf(user), lessonId);
    }

    // Salvataggio automatico. 409 se la nota è cambiata in un'altra scheda; 204 se il testo vuoto l'ha cancellata
    @PutMapping("/lessons/{lessonId}/note")
    public ResponseEntity<LessonNoteResponseDTO> saveNote(@AuthenticationPrincipal User user, @PathVariable long lessonId,
                                                          @RequestBody @Valid LessonNoteDTO body) {
        LessonNoteResponseDTO note = notebookService.saveNote(currentStudent.idOf(user), lessonId, body);
        return note == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(note);
    }

    @DeleteMapping("/lessons/{lessonId}/note")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@AuthenticationPrincipal User user, @PathVariable long lessonId) {
        notebookService.deleteNote(currentStudent.idOf(user), lessonId);
    }

    // Body: { "color": "verde" } oppure { "color": null } per il colore predefinito
    @PutMapping("/cover")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cover(@AuthenticationPrincipal User user, @RequestBody @Valid CoverColorDTO body) {
        notebookService.setCoverColor(currentStudent.idOf(user), body.color());
    }

    // "Salva nel Quaderno" di una Risorsa: un segnalibro (§7.4). Idempotente
    @PutMapping("/bookmarks/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addBookmark(@AuthenticationPrincipal User user, @PathVariable long resourceId) {
        notebookService.addBookmark(currentStudent.idOf(user), resourceId);
    }

    @DeleteMapping("/bookmarks/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBookmark(@AuthenticationPrincipal User user, @PathVariable long resourceId) {
        notebookService.removeBookmark(currentStudent.idOf(user), resourceId);
    }
}
