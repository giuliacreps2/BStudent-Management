package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.GamificationSetting;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.gamification.LifeAwardReason;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.DailyChallengeDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PauseSuggestionDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeRewardsDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeRunStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StreakDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.LifeAwardsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IDailyGoalsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IGamificationSettingsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.utilities.GameRules;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.PracticeOutcome;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.PracticeRun;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.Streak;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class DailyGoalsService implements IDailyGoalsService {

    // Il giorno segue l'ora italiana (blueprint §7.3)
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    private final ExerciseAssessmentsRepository assessmentsRepository;
    private final LifeAwardsRepository lifeAwardsRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;
    private final ResourcesRepository resourcesRepository;
    private final ILivesService livesService;
    private final IGamificationSettingsService settingsService;

    public DailyGoalsService(ExerciseAssessmentsRepository assessmentsRepository, LifeAwardsRepository lifeAwardsRepository,
                             PracticeExercisesRepository practiceExercisesRepository,
                             ResourcesRepository resourcesRepository, ILivesService livesService,
                             IGamificationSettingsService settingsService) {
        this.assessmentsRepository = assessmentsRepository;
        this.lifeAwardsRepository = lifeAwardsRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
        this.resourcesRepository = resourcesRepository;
        this.livesService = livesService;
        this.settingsService = settingsService;
    }

    // Un tentativo valutato della Palestra, con il suo identificativo
    private record Evaluated(long attemptId, PracticeOutcome outcome) {
    }

    @Override
    @Transactional(readOnly = true)
    public DailyChallengeDTO getDailyChallenge(long studentId) {
        return dailyChallenge(studentId, outcomes(evaluated(studentId)), today(), settingsService.get());
    }

    @Override
    @Transactional(readOnly = true)
    public StreakDTO getStreak(long studentId) {
        List<LocalDate> days = assessmentsRepository.findStudyDays(studentId).stream().map(LocalDate::parse).toList();
        Streak streak = GameRules.streakOf(days, today());
        return new StreakDTO(streak.current(), streak.record());
    }

    @Override
    @Transactional(readOnly = true)
    public PracticeRunStateDTO getPracticeRunState(long studentId) {
        return runState(evaluated(studentId), settingsService.get());
    }

    @Override
    @Transactional
    public PracticeRewardsDTO onPracticeAttemptEvaluated(long studentId, long exerciseAttemptId) {
        List<Evaluated> all = evaluated(studentId);
        int index = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).attemptId() == exerciseAttemptId) {
                index = i;
            }
        }
        if (index < 0) {
            throw new NotFoundException("Il tentativo " + exerciseAttemptId + " non è un esercizio valutato della Palestra di questo studente");
        }
        GamificationSetting settings = settingsService.get();
        // Lo stato prima e dopo questo tentativo: il premio nasce dal tentativo che completa il traguardo
        List<Evaluated> upToThis = all.subList(0, index + 1);
        List<PracticeOutcome> before = outcomes(all.subList(0, index));
        List<PracticeOutcome> after = outcomes(upToThis);
        LocalDate day = all.get(index).outcome().day();
        int streakForLife = settings.getPracticeStreakForLife();

        boolean streakLife = GameRules.practiceRun(after, streakForLife).livesEarned()
                > GameRules.practiceRun(before, streakForLife).livesEarned();
        LivesDTO lives = null;
        if (streakLife) {
            // L'evento è il tentativo: una seconda chiamata non assegna un'altra vita
            lives = livesService.awardLife(studentId, LifeAwardReason.practice_streak, day, "practice_streak:" + exerciseAttemptId);
        }
        int target = settings.getDailyChallengeTarget();
        boolean challengeLife = GameRules.passedToday(after, day) >= target && GameRules.passedToday(before, day) < target;
        if (challengeLife) {
            lives = livesService.awardLife(studentId, LifeAwardReason.daily_challenge, day,
                    "daily_challenge:" + studentId + ":" + day);
        }
        if (lives == null) {
            lives = livesService.getLives(studentId);
        }
        return new PracticeRewardsDTO(streakLife, challengeLife, dailyChallenge(studentId, after, day, settings),
                runState(upToThis, settings), lives);
    }

    private DailyChallengeDTO dailyChallenge(long studentId, List<PracticeOutcome> outcomes, LocalDate day,
                                             GamificationSetting settings) {
        int passed = GameRules.passedToday(outcomes, day);
        int target = settings.getDailyChallengeTarget();
        return new DailyChallengeDTO(Math.min(passed, target), target, passed >= target,
                lifeAwardsRepository.existsByStudentIdAndReasonAndAwardDay(studentId, LifeAwardReason.daily_challenge, day));
    }

    private PracticeRunStateDTO runState(List<Evaluated> evaluated, GamificationSetting settings) {
        PracticeRun run = GameRules.practiceRun(outcomes(evaluated), settings.getPracticeStreakForLife());
        // L'invito si ripete ogni N errori di fila, non a ogni errore dopo il terzo
        boolean suggestPause = run.failedInARow() > 0 && run.failedInARow() % settings.getPracticeFailsForPause() == 0;
        return new PracticeRunStateDTO(run.inARow(), settings.getPracticeStreakForLife(),
                suggestPause ? pause(evaluated.get(evaluated.size() - 1).outcome().practiceExerciseId()) : null);
    }

    // La teoria da rileggere è la Risorsa dell'argomento dell'ultimo esercizio sbagliato (§8.1-bis)
    private PauseSuggestionDTO pause(long practiceExerciseId) {
        PracticeExercise exercise = practiceExercisesRepository.findById(practiceExerciseId).orElse(null);
        if (exercise == null) {
            return new PauseSuggestionDTO("Oggi sembra dura. Fai una pausa e riprova più tardi.", null, null, null, null);
        }
        Topic topic = exercise.getTopic();
        Resource resource = resourcesRepository
                .findFirstByTopic_TopicIdAndStatusOrderByResourceId(topic.getTopicId(), ContentStatus.published).orElse(null);
        return new PauseSuggestionDTO("Oggi sembra dura. Rileggi la teoria di questo argomento e riprova.",
                topic.getTopicName(), topic.getSubject().getSlug(),
                resource == null ? null : resource.getSlug(), resource == null ? null : resource.getTitle());
    }

    private List<Evaluated> evaluated(long studentId) {
        List<Evaluated> result = new ArrayList<>();
        for (Object[] row : assessmentsRepository.findPracticeOutcomes(studentId)) {
            LocalDate day = Instant.ofEpochMilli((long) (((Number) row[2]).doubleValue() * 1000)).atZone(ROME).toLocalDate();
            // inProgram sempre vero finché la classe dello studente non esiste (TODO nell'interfaccia)
            result.add(new Evaluated(((Number) row[0]).longValue(),
                    new PracticeOutcome(((Number) row[1]).longValue(), day, Boolean.TRUE.equals(row[3]), true)));
        }
        return result;
    }

    private static List<PracticeOutcome> outcomes(List<Evaluated> evaluated) {
        return evaluated.stream().map(Evaluated::outcome).toList();
    }

    private static LocalDate today() {
        return LocalDate.now(ROME);
    }
}
