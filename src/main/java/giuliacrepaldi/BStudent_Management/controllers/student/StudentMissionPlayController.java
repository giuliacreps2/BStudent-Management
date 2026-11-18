package giuliacrepaldi.BStudent_Management.controllers.student;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.CurrentMissionDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionPlayDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionStepOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionSyncDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.attempts.interfaces.IMissionRunsService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Svolgimento delle missioni: lo studente si ricava dal token.
// Body delle risposte: { "selectedAnswerId": 12 } per la scelta multipla, { "text": "..." } per gli altri tipi
@RestController
@RequestMapping("/student")
public class StudentMissionPlayController {

    private final CurrentStudent currentStudent;
    private final IMissionRunsService missionRunsService;

    public StudentMissionPlayController(CurrentStudent currentStudent, IMissionRunsService missionRunsService) {
        this.currentStudent = currentStudent;
        this.missionRunsService = missionRunsService;
    }

    // "Inizia" / "Riprendi" / "Riprova" dalla pagina della missione: 409 se bloccata, senza vite, in attesa della
    // docente o con un'altra missione della stessa materia in corso; 403 se è dell'abbonamento
    @PostMapping("/courses/{courseSlug}/missions/{missionSlug}/play")
    public MissionPlayDTO start(@AuthenticationPrincipal User user, @PathVariable String courseSlug,
                                @PathVariable String missionSlug) {
        return missionRunsService.start(currentStudent.idOf(user), courseSlug, missionSlug);
    }

    // "Continua la missione" nella home: una per materia
    @GetMapping("/missions/current")
    public List<CurrentMissionDTO> current(@AuthenticationPrincipal User user) {
        return missionRunsService.getCurrent(currentStudent.idOf(user));
    }

    @GetMapping("/missions/attempts/{missionAttemptId}")
    public MissionPlayDTO state(@AuthenticationPrincipal User user, @PathVariable long missionAttemptId) {
        return missionRunsService.getState(currentStudent.idOf(user), missionAttemptId);
    }

    // "Ho finito di leggere"
    @PostMapping("/missions/attempts/{missionAttemptId}/lessons/{lessonId}/done")
    public MissionStepOutcomeDTO completeLesson(@AuthenticationPrincipal User user, @PathVariable long missionAttemptId,
                                                @PathVariable long lessonId) {
        return missionRunsService.completeLesson(currentStudent.idOf(user), missionAttemptId, lessonId);
    }

    // "Salva missione" e chiusura della pagina (il body con la risposta è facoltativo)
    @PostMapping("/missions/attempts/{missionAttemptId}/pause")
    public MissionPlayDTO pause(@AuthenticationPrincipal User user, @PathVariable long missionAttemptId,
                                @RequestBody(required = false) @Valid AnswerDTO body) {
        return missionRunsService.pause(currentStudent.idOf(user), missionAttemptId, body);
    }

    @PostMapping("/missions/attempts/{missionAttemptId}/resume")
    public MissionPlayDTO resume(@AuthenticationPrincipal User user, @PathVariable long missionAttemptId) {
        return missionRunsService.resume(currentStudent.idOf(user), missionAttemptId);
    }

    // "Abbandona missione": una vita persa, si riparte da zero
    @PostMapping("/missions/attempts/{missionAttemptId}/abandon")
    public LivesDTO abandon(@AuthenticationPrincipal User user, @PathVariable long missionAttemptId) {
        return missionRunsService.abandon(currentStudent.idOf(user), missionAttemptId);
    }

    // Salvataggio automatico ogni pochi secondi mentre l'esercizio è aperto
    @PutMapping("/missions/exercises/{exerciseAttemptId}/sync")
    public MissionSyncDTO sync(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId,
                               @RequestBody @Valid AnswerDTO body) {
        return missionRunsService.sync(currentStudent.idOf(user), exerciseAttemptId, body);
    }

    // Consegna: con la correzione automatica la risposta contiene subito l'esito, i punti e il passo dopo
    @PostMapping("/missions/exercises/{exerciseAttemptId}/submit")
    public MissionStepOutcomeDTO submit(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId,
                                        @RequestBody @Valid AnswerDTO body) {
        return missionRunsService.submitExercise(currentStudent.idOf(user), exerciseAttemptId, body);
    }

    // "Annulla invio": 409 se la docente ha già corretto
    @PostMapping("/missions/exercises/{exerciseAttemptId}/cancel-submission")
    public MissionPlayDTO cancelSubmission(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId) {
        return missionRunsService.cancelSubmission(currentStudent.idOf(user), exerciseAttemptId);
    }
}
