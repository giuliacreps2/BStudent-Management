package giuliacrepaldi.BStudent_Management.controllers.student;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.AttemptDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.AttemptHistoryItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.CourseProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.GrowthDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.PracticeProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.WeakPointsDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IProgressQueryService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IProgressQueryService.HistoryContext;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IProgressQueryService.HistoryOutcome;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// "I miei progressi" (blueprint §10.5): cinque gruppi, ognuno con il suo dettaglio. subject facoltativo:
// la prima materia pubblicata
@RestController
@RequestMapping("/student/progress")
public class StudentProgressController {

    private final CurrentStudent currentStudent;
    private final IProgressQueryService progressService;

    public StudentProgressController(CurrentStudent currentStudent, IProgressQueryService progressService) {
        this.currentStudent = currentStudent;
        this.progressService = progressService;
    }

    @GetMapping("/growth")
    public GrowthDTO growth(@AuthenticationPrincipal User user) {
        return progressService.getGrowth(currentStudent.idOf(user));
    }

    @GetMapping("/courses")
    public List<CourseProgressDTO> courses(@AuthenticationPrincipal User user,
                                           @RequestParam(required = false) String subject) {
        return progressService.getCourses(currentStudent.idOf(user), subject);
    }

    @GetMapping("/practice")
    public PracticeProgressDTO practice(@AuthenticationPrincipal User user,
                                        @RequestParam(required = false) String subject) {
        return progressService.getPractice(currentStudent.idOf(user), subject);
    }

    // context: all | mission | practice; outcome: all | passed | failed; allTime=true oltre gli ultimi 30 giorni
    @GetMapping("/history")
    public PagedModel<AttemptHistoryItemDTO> history(@AuthenticationPrincipal User user,
                                                     @RequestParam(defaultValue = "all") HistoryContext context,
                                                     @RequestParam(defaultValue = "all") HistoryOutcome outcome,
                                                     @RequestParam(defaultValue = "false") boolean allTime,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return new PagedModel<>(progressService.getHistory(currentStudent.idOf(user), context, outcome, allTime, page, size));
    }

    // Dettaglio di un tentativo consegnato: risposta, esito e commento (anche della docente)
    @GetMapping("/attempts/{exerciseAttemptId}")
    public AttemptDetailDTO attempt(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId) {
        return progressService.getAttempt(currentStudent.idOf(user), exerciseAttemptId);
    }

    @GetMapping("/weak-points")
    public WeakPointsDTO weakPoints(@AuthenticationPrincipal User user, @RequestParam(required = false) String subject) {
        return progressService.getWeakPoints(currentStudent.idOf(user), subject);
    }
}
