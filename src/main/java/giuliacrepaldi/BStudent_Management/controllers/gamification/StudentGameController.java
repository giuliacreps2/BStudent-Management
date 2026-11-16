package giuliacrepaldi.BStudent_Management.controllers.gamification;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Dati di gioco dello studente autenticato: lo studente si ricava dal token, mai dal client.
// 403 se l'utente non è uno studente
@RestController
@RequestMapping("/student/game")
public class StudentGameController {

    private final CurrentStudent currentStudent;
    private final ILivesService livesService;
    private final ISkillLevelsService skillLevelsService;
    private final IStudentXpService studentXpService;

    public StudentGameController(CurrentStudent currentStudent, ILivesService livesService,
                                 ISkillLevelsService skillLevelsService, IStudentXpService studentXpService) {
        this.currentStudent = currentStudent;
        this.livesService = livesService;
        this.skillLevelsService = skillLevelsService;
        this.studentXpService = studentXpService;
    }

    // Vite, base e ora della ricarica
    @GetMapping("/lives")
    public LivesDTO lives(@AuthenticationPrincipal User user) {
        return livesService.getLives(currentStudent.idOf(user));
    }

    // Livelli delle skill nella materia scelta, nell'ordine dell'autrice
    @GetMapping("/skill-levels")
    public List<SkillLevelStateDTO> skillLevels(@AuthenticationPrincipal User user, @RequestParam long subjectId) {
        return skillLevelsService.getLevels(currentStudent.idOf(user), subjectId);
    }

    // XP, stadio attuale e che cosa manca alla prossima evoluzione
    @GetMapping("/evolution")
    public EvolutionProgressDTO evolution(@AuthenticationPrincipal User user) {
        return studentXpService.getEvolutionProgress(currentStudent.idOf(user));
    }
}
