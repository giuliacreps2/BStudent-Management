package giuliacrepaldi.BStudent_Management.controllers.student;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.progression.StudyEventDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IStudyEventsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Registro di studio (blueprint §10.5): tratti di lettura e consigli aperti, inviati dal frontend
@RestController
@RequestMapping("/student/study-events")
public class StudentStudyEventsController {

    private final CurrentStudent currentStudent;
    private final IStudyEventsService studyEventsService;

    public StudentStudyEventsController(CurrentStudent currentStudent, IStudyEventsService studyEventsService) {
        this.currentStudent = currentStudent;
        this.studyEventsService = studyEventsService;
    }

    // Body: { "kind": "lesson_read", "lessonId": 12, "lessonPageId": 40, "startedAt": "...", "activeMs": 30000 }
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void record(@AuthenticationPrincipal User user, @RequestBody @Valid StudyEventDTO body) {
        studyEventsService.record(currentStudent.idOf(user), body);
    }
}
