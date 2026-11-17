package giuliacrepaldi.BStudent_Management.controllers.student;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.PracticeAttemptDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.attempts.interfaces.IPracticeAttemptsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Svolgimento degli esercizi della Palestra: lo studente si ricava dal token.
// Body delle risposte: { "selectedAnswerId": 12 } per la scelta multipla, { "text": "..." } per gli altri tipi
@RestController
@RequestMapping("/student/practice")
public class StudentPracticeAttemptController {

    private final CurrentStudent currentStudent;
    private final IPracticeAttemptsService attemptsService;

    public StudentPracticeAttemptController(CurrentStudent currentStudent, IPracticeAttemptsService attemptsService) {
        this.currentStudent = currentStudent;
        this.attemptsService = attemptsService;
    }

    // Avvia o riprende: 409 se la voce è bloccata per livello o è in correzione, 403 se è dell'abbonamento
    @PostMapping("/exercises/{practiceExerciseId}/attempts")
    public PracticeAttemptDTO start(@AuthenticationPrincipal User user, @PathVariable long practiceExerciseId) {
        return attemptsService.start(currentStudent.idOf(user), practiceExerciseId);
    }

    @GetMapping("/attempts/{exerciseAttemptId}")
    public PracticeAttemptDTO get(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId) {
        return attemptsService.getAttempt(currentStudent.idOf(user), exerciseAttemptId);
    }

    // "Salva esercizio"
    @PutMapping("/attempts/{exerciseAttemptId}/draft")
    public PracticeAttemptDTO saveDraft(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId,
                                        @RequestBody @Valid AnswerDTO body) {
        return attemptsService.saveDraft(currentStudent.idOf(user), exerciseAttemptId, body);
    }

    // "Esci senza salvare"
    @PostMapping("/attempts/{exerciseAttemptId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId) {
        attemptsService.leave(currentStudent.idOf(user), exerciseAttemptId);
    }

    // Consegna: con la correzione automatica la risposta contiene subito l'esito, i punti e i premi
    @PostMapping("/attempts/{exerciseAttemptId}/submit")
    public PracticeAttemptDTO submit(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId,
                                     @RequestBody @Valid AnswerDTO body) {
        return attemptsService.submit(currentStudent.idOf(user), exerciseAttemptId, body);
    }

    // "Annulla invio": 409 se la docente ha già corretto
    @PostMapping("/attempts/{exerciseAttemptId}/cancel-submission")
    public PracticeAttemptDTO cancelSubmission(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId) {
        return attemptsService.cancelSubmission(currentStudent.idOf(user), exerciseAttemptId);
    }
}
