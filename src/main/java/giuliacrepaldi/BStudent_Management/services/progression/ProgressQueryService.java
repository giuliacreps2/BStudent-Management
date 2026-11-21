package giuliacrepaldi.BStudent_Management.services.progression;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.progression.CourseResult;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import giuliacrepaldi.BStudent_Management.enums.gamification.AccessoryStatus;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseViewDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StudentAccessoryDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.AttemptDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.AttemptHistoryItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.CourseProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.GrowthDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.PracticeProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.PracticeProgressItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.SubjectSkillsDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.WeakPointsDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.ProgressDTOs.WeakTopicDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.CourseCardDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeItemDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.CourseResultsRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.assessment.interfaces.IAssessmentsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IAccessoriesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IProgressQueryService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISkinsService;
import giuliacrepaldi.BStudent_Management.services.student.StudentVisibility;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentCatalogService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentPracticeService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentTestsService;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProgressQueryService implements IProgressQueryService {

    /** Storico: di default gli ultimi 30 giorni, il resto a richiesta (§10.5) */
    static final int HISTORY_DAYS = 30;
    static final int MAX_PAGE_SIZE = 50;
    /** Accessori vicini mostrati nella crescita */
    static final int NEAR_ACCESSORIES = 3;

    private static final Set<ExerciseAttemptStatus> SUBMITTED = Set.of(
            ExerciseAttemptStatus.assessed, ExerciseAttemptStatus.pending_review, ExerciseAttemptStatus.expired);

    private final SubjectsRepository subjectsRepository;
    private final ExerciseAttemptsRepository attemptsRepository;
    private final ExerciseAssessmentsRepository assessmentsRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;
    private final CourseResultsRepository courseResultsRepository;
    private final ResourcesRepository resourcesRepository;
    private final IStudentXpService xpService;
    private final ISkinsService skinsService;
    private final ISkillLevelsService skillLevelsService;
    private final IAccessoriesService accessoriesService;
    private final IStudentCatalogService catalogService;
    private final IStudentPracticeService practiceService;
    private final IStudentTestsService testsService;
    private final IAssessmentsService assessmentsService;

    public ProgressQueryService(SubjectsRepository subjectsRepository, ExerciseAttemptsRepository attemptsRepository,
                                ExerciseAssessmentsRepository assessmentsRepository,
                                PracticeExercisesRepository practiceExercisesRepository,
                                CourseResultsRepository courseResultsRepository, ResourcesRepository resourcesRepository,
                                IStudentXpService xpService, ISkinsService skinsService,
                                ISkillLevelsService skillLevelsService, IAccessoriesService accessoriesService,
                                IStudentCatalogService catalogService, IStudentPracticeService practiceService,
                                IStudentTestsService testsService, IAssessmentsService assessmentsService) {
        this.subjectsRepository = subjectsRepository;
        this.attemptsRepository = attemptsRepository;
        this.assessmentsRepository = assessmentsRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
        this.courseResultsRepository = courseResultsRepository;
        this.resourcesRepository = resourcesRepository;
        this.xpService = xpService;
        this.skinsService = skinsService;
        this.skillLevelsService = skillLevelsService;
        this.accessoriesService = accessoriesService;
        this.catalogService = catalogService;
        this.practiceService = practiceService;
        this.testsService = testsService;
        this.assessmentsService = assessmentsService;
    }

    // ------------------------------------------------------------------ 1. Crescita

    @Override
    @Transactional(readOnly = true)
    public GrowthDTO getGrowth(long studentId) {
        List<SubjectSkillsDTO> subjects = subjectsRepository.findByStatusOrderBySubjectId(SubjectStatus.published).stream()
                .map(s -> new SubjectSkillsDTO(s.getSlug(), s.getSubjectName(),
                        skillLevelsService.getLevels(studentId, s.getSubjectId())))
                .toList();
        return new GrowthDTO(xpService.getEvolutionProgress(studentId), skinsService.getCurrentCharacter(studentId),
                subjects, nearAccessories(accessoriesService.getAccessories(studentId)));
    }

    /** Accessori non ancora posseduti più vicini: prima i riscattabili, poi i bloccati con più punti in proporzione */
    static List<StudentAccessoryDTO> nearAccessories(List<StudentAccessoryDTO> accessories) {
        return accessories.stream()
                .filter(a -> a.status() == AccessoryStatus.redeemable || a.status() == AccessoryStatus.locked)
                .sorted(Comparator.comparing((StudentAccessoryDTO a) -> a.status() != AccessoryStatus.redeemable)
                        .thenComparing(ProgressQueryService::closeness, Comparator.reverseOrder()))
                .limit(NEAR_ACCESSORIES)
                .toList();
    }

    private static BigDecimal closeness(StudentAccessoryDTO accessory) {
        if (accessory.requiredPoints() == null || accessory.requiredPoints().signum() <= 0
                || accessory.currentPoints() == null) {
            return BigDecimal.ZERO;
        }
        return accessory.currentPoints().divide(accessory.requiredPoints(), 4, RoundingMode.HALF_UP);
    }

    // ------------------------------------------------------------------ 2. Corsi

    @Override
    @Transactional(readOnly = true)
    public List<CourseProgressDTO> getCourses(long studentId, String subjectSlug) {
        Subject subject = findSubject(subjectSlug);
        if (subject == null) {
            return List.of();
        }
        Map<String, CourseResult> results = courseResultsRepository.findByStudent(studentId).stream()
                .collect(Collectors.toMap(r -> r.getCourse().getSlug(), Function.identity()));
        return catalogService.getCoursesPage(studentId, subject.getSlug()).courses().stream()
                .map(course -> toCourseProgress(course, results.get(course.slug())))
                .toList();
    }

    private static CourseProgressDTO toCourseProgress(CourseCardDTO course, CourseResult result) {
        if (result == null) {
            return new CourseProgressDTO(course.slug(), course.title(), 0, course.stepsCount(), null, false);
        }
        // Le missioni visibili possono essere cambiate dopo l'ultimo risultato: vale il numero attuale
        int total = Math.max(course.stepsCount(), result.getPassedMissions());
        return new CourseProgressDTO(course.slug(), course.title(), result.getPassedMissions(), total,
                result.getPercentage(), result.getCompletedAt() != null);
    }

    // ------------------------------------------------------------------ 3. Palestra

    @Override
    @Transactional(readOnly = true)
    public PracticeProgressDTO getPractice(long studentId, String subjectSlug) {
        Subject subject = findSubject(subjectSlug);
        if (subject == null) {
            return new PracticeProgressDTO(List.of(), List.of());
        }
        Set<Long> inSubject = practiceExercisesRepository.findVisibleBySubject(subject.getSubjectId()).stream()
                .map(PracticeExercise::getPracticeExerciseId).collect(Collectors.toSet());

        // Righe: voce, miglior percentuale, superata, bozza, in correzione, ultima attività
        Map<Long, BigDecimal> best = new HashMap<>();
        for (Object[] row : practiceExercisesRepository.findPersonalStates(studentId)) {
            long id = ((Number) row[0]).longValue();
            if (inSubject.contains(id)) {
                best.put(id, row[1] == null ? null : new BigDecimal(row[1].toString()));
            }
        }
        // Righe: voce, tentativi valutati, ultimo tentativo valutato
        Map<Long, Object[]> stats = attemptsRepository.findPracticeAttemptStats(studentId).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), Function.identity()));

        List<PracticeItemDTO> items = practiceService.getByIds(studentId, List.copyOf(best.keySet()));
        List<PracticeProgressItemDTO> done = items.stream()
                .filter(item -> !"in_review".equals(item.status()))
                .map(item -> {
                    Object[] row = stats.get(item.practiceExerciseId());
                    int count = row == null ? 0 : ((Number) row[1]).intValue();
                    Long last = row == null || row[2] == null ? null : ((Number) row[2]).longValue();
                    return new PracticeProgressItemDTO(item, best.get(item.practiceExerciseId()), count, last);
                })
                .sorted(Comparator.comparing((PracticeProgressItemDTO p) -> p.item().lastActivityAt(),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        List<PracticeItemDTO> inReview = items.stream().filter(item -> "in_review".equals(item.status())).toList();
        return new PracticeProgressDTO(done, inReview);
    }

    // ------------------------------------------------------------------ 4. Storico

    @Override
    @Transactional(readOnly = true)
    public Page<AttemptHistoryItemDTO> getHistory(long studentId, HistoryContext context, HistoryOutcome outcome,
                                                  boolean allTime, int page, int size) {
        OffsetDateTime from = allTime ? OffsetDateTime.parse("2000-01-01T00:00:00Z")
                : OffsetDateTime.now().minusDays(HISTORY_DAYS);
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return attemptsRepository.findHistory(studentId, SUBMITTED, from, context.ordinal(), outcome.ordinal(),
                        PageRequest.of(Math.max(page, 0), pageSize))
                .map(row -> toHistoryItem((ExerciseAttempt) row[0], (ExerciseAssessment) row[1]));
    }

    private AttemptHistoryItemDTO toHistoryItem(ExerciseAttempt attempt, ExerciseAssessment assessment) {
        boolean mission = attempt.getContext() == ExerciseContext.mission;
        String place = mission
                ? "Missione «" + attempt.getMissionAttempt().getMissionRun().getMission().getMissionTitle() + "»"
                : "Palestra";
        String subjectName = mission
                ? attempt.getMissionAttempt().getMissionRun().getSubject().getSubjectName()
                : subjectOf(attempt);
        String title = Objects.requireNonNullElse(attempt.getQuestionBankEntry().getTitle(), "Esercizio");
        OffsetDateTime date = attempt.getSubmittedAt() != null ? attempt.getSubmittedAt()
                : attempt.getCompletedAt() != null ? attempt.getCompletedAt() : attempt.getStartedAt();
        BigDecimal percentage = assessment == null ? null : assessment.getPercentage();
        Boolean passed = assessment == null ? null
                : assessment.getPercentage().compareTo(assessment.getPassingThreshold()) >= 0;
        return new AttemptHistoryItemDTO(attempt.getExerciseAttemptId(), attempt.getContext(), title, place,
                subjectName, date, attempt.getStatus().name(), percentage, grade(percentage), passed,
                assessment != null && assessment.getAssessmentMethod() == AssessmentMethod.teacher);
    }

    /** Voto in decimi arrotondato al mezzo punto più vicino (§10.2): 83% -> 8,5; 87% -> 8,5; 88% -> 9 */
    static BigDecimal grade(BigDecimal percentage) {
        if (percentage == null) {
            return null;
        }
        // decimi * 2, arrotondato all'intero (metà verso l'alto), poi diviso 2
        BigDecimal doubled = percentage.divide(BigDecimal.valueOf(5), 0, RoundingMode.HALF_UP);
        return doubled.divide(BigDecimal.valueOf(2), 1, RoundingMode.UNNECESSARY);
    }

    private String subjectOf(ExerciseAttempt attempt) {
        Topic topic = attempt.getQuestionBankEntry().getTopic();
        if (topic != null) {
            return topic.getSubject().getSubjectName();
        }
        return practiceExercisesRepository
                .findByQuestionBankEntry_QuestionBankEntryId(attempt.getQuestionBankEntry().getQuestionBankEntryId())
                .map(pe -> pe.getTopic().getSubject().getSubjectName()).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public AttemptDetailDTO getAttempt(long studentId, long exerciseAttemptId) {
        ExerciseAttempt attempt = attemptsRepository.findById(exerciseAttemptId)
                .filter(a -> a.getStudent().getStudentId().equals(studentId) && SUBMITTED.contains(a.getStatus()))
                .orElseThrow(() -> new NotFoundException("Tentativo non trovato"));
        ExerciseAssessment assessment = assessmentsRepository.findById(exerciseAttemptId).orElse(null);
        ExerciseViewDTO view = assessmentsService.toView(attempt);
        return new AttemptDetailDTO(toHistoryItem(attempt, assessment), view, answerText(attempt, view),
                // Feedback della docente compreso; la soluzione segue le regole dello svolgimento (§3)
                assessment == null ? null : assessmentsService.toOutcome(attempt, assessment));
    }

    /** La risposta come testo: per la scelta multipla il testo dell'opzione scelta */
    static String answerText(ExerciseAttempt attempt, ExerciseViewDTO view) {
        AnswerDTO answer = AttemptJson.readAnswer(attempt.getResponse());
        if (answer.selectedAnswerId() != null && view.options() != null) {
            return view.options().stream().filter(o -> o.answerId().equals(answer.selectedAnswerId()))
                    .map(ExerciseViewDTO.Option::text).findFirst().orElse(null);
        }
        return answer.text();
    }

    // ------------------------------------------------------------------ 5. Punti deboli

    @Override
    @Transactional(readOnly = true)
    public WeakPointsDTO getWeakPoints(long studentId, String subjectSlug) {
        Subject subject = findSubject(subjectSlug);
        if (subject == null) {
            return new WeakPointsDTO(null, List.of(), null, null);
        }
        SkillLevelStateDTO weakest = skillLevelsService.getLevels(studentId, subject.getSubjectId()).stream()
                .min(Comparator.comparingInt(SkillLevelStateDTO::level).thenComparing(SkillLevelStateDTO::points))
                .orElse(null);
        List<WeakTopicDTO> topics = assessmentsRepository.findWeakTopics(studentId, subject.getSubjectId()).stream()
                .map(row -> {
                    long topicId = ((Number) row[0]).longValue();
                    Resource resource = resourcesRepository
                            .findFirstByTopic_TopicIdAndStatusOrderByResourceId(topicId, ContentStatus.published)
                            .filter(StudentVisibility::isVisible).orElse(null);
                    return new WeakTopicDTO(topicId, (String) row[1], ((Number) row[2]).intValue(), subject.getSlug(),
                            resource == null ? null : resource.getSlug(), resource == null ? null : resource.getTitle());
                })
                .toList();
        return new WeakPointsDTO(weakest, topics,
                testsService.getLastResult(studentId, subject.getSlug(), OrientationTestKind.course_level),
                testsService.getLastResult(studentId, subject.getSlug(), OrientationTestKind.exercises));
    }

    // ------------------------------------------------------------------ Utilità

    /** Materia indicata (visibile) oppure la prima pubblicata; null se non ce ne sono */
    private Subject findSubject(String subjectSlug) {
        if (subjectSlug != null && !subjectSlug.isBlank()) {
            return subjectsRepository.findBySlug(subjectSlug)
                    .filter(StudentVisibility::isVisible)
                    .orElseThrow(() -> new NotFoundException("Materia non trovata"));
        }
        List<Subject> subjects = subjectsRepository.findByStatusOrderBySubjectId(SubjectStatus.published);
        return subjects.isEmpty() ? null : subjects.get(0);
    }
}
