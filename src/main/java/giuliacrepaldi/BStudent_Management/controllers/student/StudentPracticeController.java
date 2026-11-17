package giuliacrepaldi.BStudent_Management.controllers.student;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.student.ExercisesPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeListDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.ResourceCardDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.ResourceDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.ResourcesHubDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentPracticeService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentResourcesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Palestra ("Esercizi") e Risorse dello studente autenticato: lo studente si ricava dal token.
// 403 se l'utente non è uno studente; 404 per i contenuti non pubblicati
@RestController
@RequestMapping("/student")
public class StudentPracticeController {

    private final CurrentStudent currentStudent;
    private final IStudentPracticeService practiceService;
    private final IStudentResourcesService resourcesService;

    public StudentPracticeController(CurrentStudent currentStudent, IStudentPracticeService practiceService,
                                     IStudentResourcesService resourcesService) {
        this.currentStudent = currentStudent;
        this.practiceService = practiceService;
        this.resourcesService = resourcesService;
    }

    // ------------------------------------------------------------------ Palestra

    // Pagina "Esercizi" (subject facoltativo: la prima materia)
    @GetMapping("/practice")
    public ExercisesPageDTO practice(@AuthenticationPrincipal User user, @RequestParam(required = false) String subject) {
        return practiceService.getExercisesPage(currentStudent.idOf(user), subject);
    }

    // Voci di una skill, raggruppate per argomento
    @GetMapping("/practice/{subjectSlug}/skills/{skillCode}")
    public PracticeListDTO bySkill(@AuthenticationPrincipal User user, @PathVariable String subjectSlug,
                                   @PathVariable String skillCode) {
        return practiceService.getPracticeBySkill(currentStudent.idOf(user), subjectSlug, skillCode);
    }

    // "Obiettivo di oggi": senza skill = "Sorprendimi" (la skill più debole)
    @GetMapping("/practice/{subjectSlug}/today-goal")
    public List<PracticeItemDTO> todayGoal(@AuthenticationPrincipal User user, @PathVariable String subjectSlug,
                                           @RequestParam(required = false) String skill) {
        return practiceService.getTodayGoal(currentStudent.idOf(user), subjectSlug, skill);
    }

    // "Vai agli esercizi" da una Risorsa
    @GetMapping("/practice/topics/{topicId}")
    public List<PracticeItemDTO> byTopic(@AuthenticationPrincipal User user, @PathVariable long topicId) {
        return practiceService.getByTopic(currentStudent.idOf(user), topicId);
    }

    // ------------------------------------------------------------------ Risorse

    @GetMapping("/resources")
    public ResourcesHubDTO resources(@AuthenticationPrincipal User user, @RequestParam(required = false) String subject) {
        return resourcesService.getHub(currentStudent.idOf(user), subject);
    }

    // Ricerca per titolo e descrizione, nella materia scelta; almeno 2 caratteri
    @GetMapping("/resources/{subjectSlug}/search")
    public List<ResourceCardDTO> search(@AuthenticationPrincipal User user, @PathVariable String subjectSlug,
                                        @RequestParam String q) {
        return resourcesService.search(currentStudent.idOf(user), subjectSlug, q);
    }

    @GetMapping("/resources/{subjectSlug}/{resourceSlug}")
    public ResourceDetailDTO resource(@AuthenticationPrincipal User user, @PathVariable String subjectSlug,
                                      @PathVariable String resourceSlug) {
        return resourcesService.getResource(currentStudent.idOf(user), subjectSlug, resourceSlug);
    }
}
