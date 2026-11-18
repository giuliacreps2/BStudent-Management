package giuliacrepaldi.BStudent_Management.controllers.assessment;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionAssessedDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionListItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.TeacherAssessmentDTO;
import giuliacrepaldi.BStudent_Management.services.assessment.interfaces.ISubmissionsService;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// "Consegne da correggere". SecurityConfig protegge /admin/academy/** (solo ADMIN); la regola su chi può
// correggere è ripetuta in ISubmissionsService.canAssess, così il futuro ruolo docente si aggiunge lì
@RestController
@RequestMapping("/admin/academy/submissions")
public class SubmissionController {

    private final ISubmissionsService submissionsService;

    public SubmissionController(ISubmissionsService submissionsService) {
        this.submissionsService = submissionsService;
    }

    // Consegne in attesa, dalla più vecchia; materia facoltativa
    @GetMapping
    public PagedModel<SubmissionListItemDTO> findPending(@AuthenticationPrincipal User user,
                                                         @RequestParam(required = false) Long subjectId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return new PagedModel<>(submissionsService.findPending(user, subjectId, page, size));
    }

    // 409 se lo studente ha annullato l'invio
    @GetMapping("/{exerciseAttemptId}")
    public SubmissionDetailDTO findById(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId) {
        return submissionsService.getSubmission(user, exerciseAttemptId);
    }

    // Body: { "percentage": 75, "feedback": "..." }. 409 se già corretta o se lo studente ha annullato l'invio
    @PostMapping("/{exerciseAttemptId}/assessment")
    public SubmissionAssessedDTO assess(@AuthenticationPrincipal User user, @PathVariable long exerciseAttemptId,
                                        @RequestBody @Valid TeacherAssessmentDTO body) {
        return submissionsService.assess(user, exerciseAttemptId, body);
    }
}
