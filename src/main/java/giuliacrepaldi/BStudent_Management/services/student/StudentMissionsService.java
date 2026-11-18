package giuliacrepaldi.BStudent_Management.services.student;

import giuliacrepaldi.BStudent_Management.entities.catalog.AcademySetting;
import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRun;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionRunStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.ForbiddenException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.MissionRewardsDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionBriefingDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionMapDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionNodeDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionStepDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionCheckpointsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionRunLessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionRunsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.AcademySettingsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionStepsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IHintsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentGameStateService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentMissionsService;
import giuliacrepaldi.BStudent_Management.utilities.MissionPath;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudentMissionsService implements IStudentMissionsService {

    private static final String COMPLETED = "completed";
    private static final String CURRENT = "current";
    private static final String LOCKED = "locked";
    private static final List<MissionRunStatus> OPEN_RUN = List.of(MissionRunStatus.in_progress, MissionRunStatus.pending_review);

    private final StudentCatalogService catalogService;
    private final MissionStepsRepository missionStepsRepository;
    private final MissionAttemptsRepository missionAttemptsRepository;
    private final MissionRunsRepository missionRunsRepository;
    private final MissionRunLessonsRepository runLessonsRepository;
    private final MissionCheckpointsRepository checkpointsRepository;
    private final AcademySettingsRepository academySettingsRepository;
    private final StudentsRepository studentsRepository;
    private final IEntitlementService entitlementService;
    private final IStudentGameStateService gameStateService;
    private final ILivesService livesService;
    private final IHintsService hintsService;

    public StudentMissionsService(StudentCatalogService catalogService, MissionStepsRepository missionStepsRepository,
                                  MissionAttemptsRepository missionAttemptsRepository,
                                  MissionRunsRepository missionRunsRepository,
                                  MissionRunLessonsRepository runLessonsRepository,
                                  MissionCheckpointsRepository checkpointsRepository,
                                  AcademySettingsRepository academySettingsRepository,
                                  StudentsRepository studentsRepository, IEntitlementService entitlementService,
                                  IStudentGameStateService gameStateService, ILivesService livesService,
                                  IHintsService hintsService) {
        this.catalogService = catalogService;
        this.missionStepsRepository = missionStepsRepository;
        this.missionAttemptsRepository = missionAttemptsRepository;
        this.missionRunsRepository = missionRunsRepository;
        this.runLessonsRepository = runLessonsRepository;
        this.checkpointsRepository = checkpointsRepository;
        this.academySettingsRepository = academySettingsRepository;
        this.studentsRepository = studentsRepository;
        this.entitlementService = entitlementService;
        this.gameStateService = gameStateService;
        this.livesService = livesService;
        this.hintsService = hintsService;
    }

    @Override
    @Transactional(readOnly = true)
    public MissionMapDTO getMissionMap(long studentId, String courseSlug) {
        Course course = catalogService.findVisibleCourse(courseSlug);
        Subject subject = course.getSubject();
        return new MissionMapDTO(course.getSlug(), course.getCourseTitle(), subject.getSlug(), subject.getSubjectName(),
                course.getWorldTheme(), course.getQuote(), course.getQuoteAuthor(), nodes(studentId, course));
    }

    // Non readOnly: leggere vite e cariche applica le ricariche scadute
    @Override
    @Transactional
    public MissionPageDTO getMissionPage(long studentId, String courseSlug, String missionSlug) {
        Course course = catalogService.findVisibleCourse(courseSlug);
        MissionNodeDTO node = nodes(studentId, course).stream()
                .filter(n -> n.slug().equals(missionSlug)).findFirst()
                .orElseThrow(() -> new NotFoundException("Missione non trovata"));
        Mission mission = catalogService.visibleMissions(course).stream()
                .filter(m -> m.getMissionId().equals(node.missionId())).findFirst().orElseThrow();

        // Tappe dal percorso aperto, se c'è; altrimenti si parte dalla prima
        MissionRun run = missionRunsRepository.findByMission(studentId, mission.getMissionId(), OPEN_RUN).stream()
                .findFirst().orElse(null);
        List<Long> readLessons = run == null ? List.of() : runLessonsRepository.findLessonIds(run.getMissionRunId());
        List<Long> passedExercises = run == null ? List.of() : checkpointsRepository.findExerciseIds(run.getMissionRunId());
        List<MissionStepDTO> steps = stepper(
                StudentVisibility.visibleSteps(missionStepsRepository.findByMissionOrdered(mission.getMissionId())),
                readLessons, passedExercises);
        Subject subject = course.getSubject();
        MissionBriefingDTO briefing = new MissionBriefingDTO(subject.getGuideName(), subject.getGuideImageReference(),
                List.copyOf(mission.getBriefingLines()), mission.getBriefingVideoProvider(), mission.getBriefingVideoIdentifier());
        MissionRewardsDTO rewards = gameStateService.getMissionRewards(mission.getMissionId());
        LivesDTO lives = livesService.getLives(studentId);
        // Si parte dalla missione attuale (o si ripete una completata), se accessibile e con almeno una vita
        boolean canStart = !LOCKED.equals(node.status()) && !node.premiumLocked() && lives.lives() > 0;
        return new MissionPageDTO(node, course.getSlug(), course.getCourseTitle(), passingThreshold(), rewards, steps,
                briefing, lives, hintsService.getCharges(studentId), canStart, isTutorialSeen(studentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Mission findPlayableMission(long studentId, String courseSlug, String missionSlug) {
        Course course = catalogService.findVisibleCourse(courseSlug);
        MissionNodeDTO node = nodes(studentId, course).stream()
                .filter(n -> n.slug().equals(missionSlug)).findFirst()
                .orElseThrow(() -> new NotFoundException("Missione non trovata"));
        if (LOCKED.equals(node.status())) {
            throw new ConflictException("Questa missione si sblocca quando superi quelle prima");
        }
        if (node.premiumLocked()) {
            throw new ForbiddenException("Questa missione fa parte dell'abbonamento");
        }
        return catalogService.visibleMissions(course).stream()
                .filter(m -> m.getMissionId().equals(node.missionId())).findFirst().orElseThrow();
    }

    @Override
    public List<MissionStepDTO> stepper(List<MissionStep> visibleSteps, List<Long> readLessonIds,
                                        List<Long> passedExerciseIds) {
        MissionPath.Position position = MissionPath.of(visibleSteps.stream()
                .map(step -> new MissionPath.Step(step.isExercise(),
                        step.isExercise() ? step.getMissionExercise().getMissionExerciseId() : step.getLesson().getLessonId()))
                .toList(), readLessonIds, passedExerciseIds);
        List<MissionStepDTO> steps = new ArrayList<>();
        for (int i = 0; i < visibleSteps.size(); i++) {
            MissionStep step = visibleSteps.get(i);
            // Titolo per lo studente: mai il titolo interno dell'esercizio (§6.1)
            steps.add(new MissionStepDTO(i + 1, step.isExercise() ? "exercise" : "lesson",
                    step.isExercise() ? step.getMissionExercise().getQuestionBankEntry().getTitle() : step.getLesson().getLessonTitle(),
                    position.statuses().get(i)));
        }
        return steps;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTutorialSeen(long studentId) {
        return findStudent(studentId).getTutorialSeenAt() != null;
    }

    @Override
    @Transactional
    public void markTutorialSeen(long studentId) {
        Student student = findStudent(studentId);
        if (student.getTutorialSeenAt() == null) {
            student.setTutorialSeenAt(OffsetDateTime.now());
        }
    }

    // Stato dall'ordine (§5.9): superate = completate, la prima non superata = attuale, le altre bloccate
    private List<MissionNodeDTO> nodes(long studentId, Course course) {
        Set<Long> passed = new HashSet<>(missionAttemptsRepository.findPassedMissionIds(studentId, course.getCourseId()));
        List<MissionNodeDTO> nodes = new ArrayList<>();
        boolean currentAssigned = false;
        int order = 1;
        for (Mission mission : catalogService.visibleMissions(course)) {
            String status;
            if (passed.contains(mission.getMissionId())) {
                status = COMPLETED;
            } else if (!currentAssigned) {
                status = CURRENT;
                currentAssigned = true;
            } else {
                status = LOCKED;
            }
            long durationMs = 0;
            for (MissionStep step : StudentVisibility.visibleSteps(missionStepsRepository.findByMissionOrdered(mission.getMissionId()))) {
                if (step.isExercise()) {
                    durationMs += step.getMissionExercise().getAllowedDurationMs();
                }
            }
            nodes.add(new MissionNodeDTO(mission.getMissionId(), mission.getSlug(), order++, mission.getMissionTitle(),
                    mission.getTagline(), mission.getMissionDescription(), mission.getCoverReference(), mission.isFinal(),
                    status, !entitlementService.canAccess(studentId, mission),
                    // La lettura delle lezioni non ha tempo (§9.1): contano solo gli esercizi
                    (int) Math.ceil(durationMs / 60000.0),
                    gameStateService.getMissionRewards(mission.getMissionId()).xp(),
                    List.copyOf(mission.getLearningGoals())));
        }
        return nodes;
    }

    private BigDecimal passingThreshold() {
        return academySettingsRepository.findAll().stream().findFirst()
                .map(AcademySetting::getPassingThreshold).orElse(null);
    }

    private Student findStudent(long studentId) {
        return studentsRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Studente " + studentId + " non trovato"));
    }
}
