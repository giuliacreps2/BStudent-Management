package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.catalog.AcademySetting;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CourseRewardsDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.DashboardGameDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.MissionRewardsDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillRewardDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.TopBarDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.AcademySettingsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.CoursesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExerciseSkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExercisesRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IAccessoriesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IDailyGoalsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IHintsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentGameStateService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISkinsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// Non readOnly dove serve: leggere vite e cariche applica le ricariche scadute
@Service
public class StudentGameStateService implements IStudentGameStateService {

    private final ILivesService livesService;
    private final IStudentXpService studentXpService;
    private final ISkillLevelsService skillLevelsService;
    private final IDailyGoalsService dailyGoalsService;
    private final IHintsService hintsService;
    private final IAccessoriesService accessoriesService;
    private final ISkinsService skinsService;
    private final MissionsRepository missionsRepository;
    private final CoursesRepository coursesRepository;
    private final MissionExercisesRepository missionExercisesRepository;
    private final MissionExerciseSkillsRepository missionExerciseSkillsRepository;
    private final AcademySettingsRepository academySettingsRepository;

    public StudentGameStateService(ILivesService livesService, IStudentXpService studentXpService,
                                   ISkillLevelsService skillLevelsService, IDailyGoalsService dailyGoalsService,
                                   IHintsService hintsService, IAccessoriesService accessoriesService,
                                   ISkinsService skinsService, MissionsRepository missionsRepository,
                                   CoursesRepository coursesRepository,
                                   MissionExercisesRepository missionExercisesRepository,
                                   MissionExerciseSkillsRepository missionExerciseSkillsRepository,
                                   AcademySettingsRepository academySettingsRepository) {
        this.livesService = livesService;
        this.studentXpService = studentXpService;
        this.skillLevelsService = skillLevelsService;
        this.dailyGoalsService = dailyGoalsService;
        this.hintsService = hintsService;
        this.accessoriesService = accessoriesService;
        this.skinsService = skinsService;
        this.missionsRepository = missionsRepository;
        this.coursesRepository = coursesRepository;
        this.missionExercisesRepository = missionExercisesRepository;
        this.missionExerciseSkillsRepository = missionExerciseSkillsRepository;
        this.academySettingsRepository = academySettingsRepository;
    }

    @Override
    @Transactional
    public TopBarDTO getTopBar(long studentId) {
        return new TopBarDTO(livesService.getLives(studentId), studentXpService.getTotalXp(studentId),
                studentXpService.getCurrentStage(studentId), dailyGoalsService.getDailyChallenge(studentId),
                dailyGoalsService.getStreak(studentId), hintsService.getCharges(studentId));
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardGameDTO getDashboard(long studentId, long subjectId) {
        return new DashboardGameDTO(skinsService.getCurrentCharacter(studentId),
                studentXpService.getEvolutionProgress(studentId), skillLevelsService.getLevels(studentId, subjectId),
                dailyGoalsService.getStreak(studentId));
    }

    @Override
    @Transactional
    public CharacterPageDTO getCharacterPage(long studentId) {
        return new CharacterPageDTO(skinsService.getRoster(studentId), skinsService.getCurrentCharacter(studentId),
                studentXpService.getEvolutionProgress(studentId), accessoriesService.getAccessories(studentId));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseRewardsDTO getCourseRewards(long studentId, long courseId) {
        if (!coursesRepository.existsById(courseId)) {
            throw new NotFoundException("Corso " + courseId + " non trovato");
        }
        // L'Evoluzione I arriva al primo corso completato: si mostra finché lo studente è allo stadio Base
        return new CourseRewardsDTO(missionExercisesRepository.sumVisibleMaxPointsByCourse(courseId),
                studentXpService.getCurrentStage(studentId) == 0);
    }

    @Override
    @Transactional(readOnly = true)
    public MissionRewardsDTO getMissionRewards(long missionId) {
        if (!missionsRepository.existsById(missionId)) {
            throw new NotFoundException("Missione " + missionId + " non trovata");
        }
        // Fattore delle missioni (10%): i punti skill di una missione valgono meno di quelli della Palestra (§10.3)
        BigDecimal factor = academySettingsRepository.findAll().stream().findFirst()
                .map(AcademySetting::getMissionSkillFactor).orElse(BigDecimal.ZERO);
        List<SkillRewardDTO> skillPoints = missionExerciseSkillsRepository.findMaxSkillPointsByMission(missionId).stream()
                .map(row -> new SkillRewardDTO(((Number) row[0]).longValue(), (String) row[1],
                        ((BigDecimal) row[2]).multiply(factor).setScale(2, RoundingMode.HALF_UP)))
                .toList();
        return new MissionRewardsDTO(missionExercisesRepository.sumVisibleMaxPoints(missionId), skillPoints);
    }
}
