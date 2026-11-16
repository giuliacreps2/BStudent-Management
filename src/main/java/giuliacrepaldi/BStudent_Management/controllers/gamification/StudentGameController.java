package giuliacrepaldi.BStudent_Management.controllers.gamification;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.DailyChallengeDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.DashboardGameDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintReviewLinkDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeRunStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StreakDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StudentAccessoryDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.TopBarDTO;
import giuliacrepaldi.BStudent_Management.security.CurrentStudent;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IAccessoriesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IDailyGoalsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IHintsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentGameStateService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISkinsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final IStudentGameStateService gameStateService;
    private final IDailyGoalsService dailyGoalsService;
    private final IAccessoriesService accessoriesService;
    private final IHintsService hintsService;
    private final ISkinsService skinsService;

    public StudentGameController(CurrentStudent currentStudent, ILivesService livesService,
                                 ISkillLevelsService skillLevelsService, IStudentXpService studentXpService,
                                 IStudentGameStateService gameStateService, IDailyGoalsService dailyGoalsService,
                                 IAccessoriesService accessoriesService, IHintsService hintsService,
                                 ISkinsService skinsService) {
        this.currentStudent = currentStudent;
        this.livesService = livesService;
        this.skillLevelsService = skillLevelsService;
        this.studentXpService = studentXpService;
        this.gameStateService = gameStateService;
        this.dailyGoalsService = dailyGoalsService;
        this.accessoriesService = accessoriesService;
        this.hintsService = hintsService;
        this.skinsService = skinsService;
    }

    // ------------------------------------------------------------------ Pagine

    // Barra fissa: vite, XP e stadio, sfida del giorno, Serie, cariche del cappello
    @GetMapping("/top-bar")
    public TopBarDTO topBar(@AuthenticationPrincipal User user) {
        return gameStateService.getTopBar(currentStudent.idOf(user));
    }

    // Dashboard: personaggio, prossima evoluzione, livelli delle skill della materia, Serie
    @GetMapping("/dashboard")
    public DashboardGameDTO dashboard(@AuthenticationPrincipal User user, @RequestParam long subjectId) {
        return gameStateService.getDashboard(currentStudent.idOf(user), subjectId);
    }

    // Pagina del personaggio: elenco, stadi, accessori
    @GetMapping("/character")
    public CharacterPageDTO characterPage(@AuthenticationPrincipal User user) {
        return gameStateService.getCharacterPage(currentStudent.idOf(user));
    }

    // ------------------------------------------------------------------ Singoli dati

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

    @GetMapping("/daily-challenge")
    public DailyChallengeDTO dailyChallenge(@AuthenticationPrincipal User user) {
        return dailyGoalsService.getDailyChallenge(currentStudent.idOf(user));
    }

    @GetMapping("/streak")
    public StreakDTO streak(@AuthenticationPrincipal User user) {
        return dailyGoalsService.getStreak(currentStudent.idOf(user));
    }

    // Voci superate di fila verso la prossima vita e, se serve, l'invito alla pausa
    @GetMapping("/practice-run")
    public PracticeRunStateDTO practiceRun(@AuthenticationPrincipal User user) {
        return dailyGoalsService.getPracticeRunState(currentStudent.idOf(user));
    }

    // ------------------------------------------------------------------ Personaggio

    // Sceglie o cambia personaggio: solo tra quelli iniziali
    @PutMapping("/character/{characterId}")
    public CharacterDTO chooseCharacter(@AuthenticationPrincipal User user, @PathVariable long characterId) {
        return skinsService.chooseCharacter(currentStudent.idOf(user), characterId);
    }

    // ------------------------------------------------------------------ Accessori

    @GetMapping("/accessories")
    public List<StudentAccessoryDTO> accessories(@AuthenticationPrincipal User user) {
        return accessoriesService.getAccessories(currentStudent.idOf(user));
    }

    // "Riscatta": 400 con i punti mancanti se la soglia non è raggiunta
    @PostMapping("/accessories/{accessoryId}/redeem")
    public StudentAccessoryDTO redeem(@AuthenticationPrincipal User user, @PathVariable long accessoryId) {
        return accessoriesService.redeem(currentStudent.idOf(user), accessoryId);
    }

    // Indossa: 409 durante una missione in corso
    @PutMapping("/accessories/{accessoryId}/equip")
    public StudentAccessoryDTO equip(@AuthenticationPrincipal User user, @PathVariable long accessoryId) {
        return accessoriesService.equip(currentStudent.idOf(user), accessoryId);
    }

    @DeleteMapping("/accessories/{accessoryId}/equip")
    public StudentAccessoryDTO unequip(@AuthenticationPrincipal User user, @PathVariable long accessoryId) {
        return accessoriesService.unequip(currentStudent.idOf(user), accessoryId);
    }

    // ------------------------------------------------------------------ Aiuto del cappello

    // Usa l'aiuto su un esercizio della missione in corso: consuma una carica solo la prima volta
    @PostMapping("/mission-attempts/{missionAttemptId}/exercises/{missionExerciseId}/hint")
    public HintDTO useHint(@AuthenticationPrincipal User user, @PathVariable long missionAttemptId,
                           @PathVariable long missionExerciseId) {
        return hintsService.useHint(currentStudent.idOf(user), missionAttemptId, missionExerciseId);
    }

    // "Ripassa in Palestra": a missione conclusa, le Risorse degli aiuti usati
    @GetMapping("/mission-attempts/{missionAttemptId}/hint-review")
    public List<HintReviewLinkDTO> hintReview(@AuthenticationPrincipal User user, @PathVariable long missionAttemptId) {
        return hintsService.getReviewLinks(currentStudent.idOf(user), missionAttemptId);
    }
}
