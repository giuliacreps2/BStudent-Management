package giuliacrepaldi.BStudent_Management.controllers.student;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.student.CourseDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.CoursesPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionMapDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionPageDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentCatalogService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentMissionsService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Corsi e missioni dello studente autenticato: lo studente si ricava dal token, gli indirizzi usano gli slug.
// 403 se l'utente non è uno studente; 404 per i contenuti non pubblicati
@RestController
@RequestMapping("/student")
public class StudentCourseController {

    private final CurrentStudent currentStudent;
    private final IStudentCatalogService catalogService;
    private final IStudentMissionsService missionsService;

    public StudentCourseController(CurrentStudent currentStudent, IStudentCatalogService catalogService,
                                   IStudentMissionsService missionsService) {
        this.currentStudent = currentStudent;
        this.catalogService = catalogService;
        this.missionsService = missionsService;
    }

    // Materie e corsi della materia attiva (subject facoltativo: la prima materia aperta)
    @GetMapping("/courses")
    public CoursesPageDTO courses(@AuthenticationPrincipal User user, @RequestParam(required = false) String subject) {
        return catalogService.getCoursesPage(currentStudent.idOf(user), subject);
    }

    @GetMapping("/courses/{courseSlug}")
    public CourseDetailDTO course(@AuthenticationPrincipal User user, @PathVariable String courseSlug) {
        return catalogService.getCourseDetail(currentStudent.idOf(user), courseSlug);
    }

    // Mappa del corso
    @GetMapping("/courses/{courseSlug}/missions")
    public MissionMapDTO missionMap(@AuthenticationPrincipal User user, @PathVariable String courseSlug) {
        return missionsService.getMissionMap(currentStudent.idOf(user), courseSlug);
    }

    @GetMapping("/courses/{courseSlug}/missions/{missionSlug}")
    public MissionPageDTO mission(@AuthenticationPrincipal User user, @PathVariable String courseSlug,
                                  @PathVariable String missionSlug) {
        return missionsService.getMissionPage(currentStudent.idOf(user), courseSlug, missionSlug);
    }

    // { "seen": true } se il tutorial della prima volta è già stato mostrato
    @GetMapping("/tutorial")
    public Map<String, Boolean> tutorial(@AuthenticationPrincipal User user) {
        return Map.of("seen", missionsService.isTutorialSeen(currentStudent.idOf(user)));
    }

    @PostMapping("/tutorial")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void tutorialSeen(@AuthenticationPrincipal User user) {
        missionsService.markTutorialSeen(currentStudent.idOf(user));
    }
}
