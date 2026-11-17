package giuliacrepaldi.BStudent_Management.controllers.student;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import giuliacrepaldi.BStudent_Management.payloads.student.StudentTestAnswersDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.StudentTestDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.StudentTestResultDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentTestsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Test dello studente autenticato: kind = course_level (livello del corso) oppure exercises (orientamento
// della Palestra). 204 quando il test non c'è o non è ancora stato svolto: il frontend nasconde il link
@RestController
@RequestMapping("/student/tests/{subjectSlug}/{kind}")
public class StudentTestController {

    private final CurrentStudent currentStudent;
    private final IStudentTestsService testsService;

    public StudentTestController(CurrentStudent currentStudent, IStudentTestsService testsService) {
        this.currentStudent = currentStudent;
        this.testsService = testsService;
    }

    @GetMapping
    public ResponseEntity<StudentTestDTO> test(@AuthenticationPrincipal User user, @PathVariable String subjectSlug,
                                               @PathVariable OrientationTestKind kind) {
        currentStudent.idOf(user);
        return ResponseEntity.ofNullable(testsService.getTest(subjectSlug, kind));
    }

    // Body: { "answers": { "<id domanda>": <id opzione>, ... } } con tutte le domande del test
    @PostMapping
    public StudentTestResultDTO submit(@AuthenticationPrincipal User user, @PathVariable String subjectSlug,
                                       @PathVariable OrientationTestKind kind, @RequestBody @Valid StudentTestAnswersDTO body) {
        return testsService.submit(currentStudent.idOf(user), subjectSlug, kind, body.answers());
    }

    @GetMapping("/last-result")
    public ResponseEntity<StudentTestResultDTO> lastResult(@AuthenticationPrincipal User user, @PathVariable String subjectSlug,
                                                           @PathVariable OrientationTestKind kind) {
        return ResponseEntity.ofNullable(testsService.getLastResult(currentStudent.idOf(user), subjectSlug, kind));
    }
}
