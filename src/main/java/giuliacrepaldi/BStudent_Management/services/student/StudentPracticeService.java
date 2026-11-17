package giuliacrepaldi.BStudent_Management.services.student;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeLockDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.ExercisesPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeListDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeSkillDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeTopicGroupDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.SkillRefDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.SubjectCardDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.CoursesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.TopicsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SubjectSkillsRepository;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IDailyGoalsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentPracticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentPracticeService implements IStudentPracticeService {

    static final String NEW = "new";
    static final String COMPLETED = "completed";
    static final String SAVED = "saved";
    static final String RETRY = "retry";
    static final String IN_REVIEW = "in_review";

    // Voci proposte dall'"Obiettivo di oggi" (blueprint §7.3)
    private static final int TODAY_GOAL_SIZE = 3;
    // Attività da riprendere mostrate nella pagina
    private static final int RESUME_SIZE = 6;

    private final SubjectsRepository subjectsRepository;
    private final CoursesRepository coursesRepository;
    private final SubjectSkillsRepository subjectSkillsRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;
    private final TopicsRepository topicsRepository;
    private final IEntitlementService entitlementService;
    private final ISkillLevelsService skillLevelsService;
    private final IDailyGoalsService dailyGoalsService;

    public StudentPracticeService(SubjectsRepository subjectsRepository, CoursesRepository coursesRepository,
                                  SubjectSkillsRepository subjectSkillsRepository,
                                  PracticeExercisesRepository practiceExercisesRepository, TopicsRepository topicsRepository,
                                  IEntitlementService entitlementService, ISkillLevelsService skillLevelsService,
                                  IDailyGoalsService dailyGoalsService) {
        this.subjectsRepository = subjectsRepository;
        this.coursesRepository = coursesRepository;
        this.subjectSkillsRepository = subjectSkillsRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
        this.topicsRepository = topicsRepository;
        this.entitlementService = entitlementService;
        this.skillLevelsService = skillLevelsService;
        this.dailyGoalsService = dailyGoalsService;
    }

    // Stato personale di una voce, dai tentativi
    private record Personal(BigDecimal best, boolean passed, boolean saved, boolean inReview, Instant lastActivity) {

        String status() {
            if (inReview) {
                return IN_REVIEW;
            }
            if (saved) {
                return SAVED;
            }
            if (passed) {
                return COMPLETED;
            }
            return best == null ? NEW : RETRY;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ExercisesPageDTO getExercisesPage(long studentId, String subjectSlug) {
        List<Subject> subjects = subjectsRepository.findByStatusOrderBySubjectId(SubjectStatus.published);
        List<SubjectCardDTO> cards = subjects.stream().map(this::card).toList();
        Subject active = subjects.stream().filter(s -> s.getSlug().equals(subjectSlug)).findFirst()
                .orElse(subjects.isEmpty() ? null : subjects.get(0));
        if (active == null) {
            return new ExercisesPageDTO(cards, null, List.of(), List.of(), dailyGoalsService.getDailyChallenge(studentId),
                    dailyGoalsService.getPracticeRunState(studentId), dailyGoalsService.getStreak(studentId));
        }
        List<PracticeItemDTO> items = items(studentId, practiceExercisesRepository.findVisibleBySubject(active.getSubjectId()));
        Map<Long, SkillLevelStateDTO> levels = levels(studentId, active.getSubjectId());
        List<PracticeSkillDTO> skills = new ArrayList<>();
        for (SubjectSkill subjectSkill : subjectSkillsRepository.findBySubjectOrdered(active.getSubjectId())) {
            Skill skill = subjectSkill.getSkill();
            skills.add(new PracticeSkillDTO(SkillRefDTO.from(skill), levels.get(skill.getSkillId()),
                    (int) items.stream().filter(item -> item.skillCode().equals(skill.getCodeSkill())).count()));
        }
        // Da riprendere: bozze, voci in correzione e da rifare, dalla più recente
        List<PracticeItemDTO> resume = items.stream()
                .filter(item -> !NEW.equals(item.status()) && !COMPLETED.equals(item.status()))
                .sorted(Comparator.comparing(PracticeItemDTO::lastActivityAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(RESUME_SIZE).toList();
        return new ExercisesPageDTO(cards, card(active), skills, resume, dailyGoalsService.getDailyChallenge(studentId),
                dailyGoalsService.getPracticeRunState(studentId), dailyGoalsService.getStreak(studentId));
    }

    @Override
    @Transactional(readOnly = true)
    public PracticeListDTO getPracticeBySkill(long studentId, String subjectSlug, String skillCode) {
        Subject subject = findSubject(subjectSlug);
        Skill skill = subjectSkillsRepository.findBySubjectOrdered(subject.getSubjectId()).stream()
                .map(SubjectSkill::getSkill).filter(s -> s.getCodeSkill().equals(skillCode)).findFirst()
                .orElseThrow(() -> new NotFoundException("Skill non trovata in questa materia"));
        List<PracticeExercise> exercises = practiceExercisesRepository.findVisibleBySubject(subject.getSubjectId()).stream()
                .filter(e -> e.getSkill().getSkillId().equals(skill.getSkillId())).toList();
        Map<Long, PracticeTopicGroupDTO> groups = new LinkedHashMap<>();
        Map<Long, PracticeExercise> byId = new HashMap<>();
        exercises.forEach(e -> byId.put(e.getPracticeExerciseId(), e));
        for (PracticeItemDTO item : items(studentId, exercises)) {
            Topic topic = byId.get(item.practiceExerciseId()).getTopic();
            groups.computeIfAbsent(topic.getTopicId(), id -> new PracticeTopicGroupDTO(topic.getTopicId(), topic.getTopicName(),
                    topic.getImageReference(), topic.getReferenceYear() == null ? null : topic.getReferenceYear().intValue(),
                    new ArrayList<>())).items().add(item);
        }
        return new PracticeListDTO(SkillRefDTO.from(skill), levels(studentId, subject.getSubjectId()).get(skill.getSkillId()),
                List.copyOf(groups.values()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PracticeItemDTO> getTodayGoal(long studentId, String subjectSlug, String skillCode) {
        Subject subject = findSubject(subjectSlug);
        // Le voci bloccate (difficoltà o premium) non si propongono
        List<PracticeItemDTO> open = items(studentId, practiceExercisesRepository.findVisibleBySubject(subject.getSubjectId()))
                .stream().filter(item -> !item.lock().locked() && !item.premiumLocked()).toList();
        String code = skillCode != null ? skillCode : weakestSkill(studentId, subject, open);
        if (code == null) {
            return List.of();
        }
        // Regola spiegabile: prima da rifare, poi mai svolte, poi le altre (§7.3)
        return open.stream()
                .filter(item -> item.skillCode().equals(code))
                .sorted(Comparator.comparingInt((PracticeItemDTO item) -> goalRank(item.status()))
                        .thenComparingInt(PracticeItemDTO::difficulty))
                .limit(TODAY_GOAL_SIZE).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PracticeItemDTO> getByTopic(long studentId, long topicId) {
        Topic topic = topicsRepository.findById(topicId)
                .filter(t -> StudentVisibility.isVisible(t.getSubject()))
                .orElseThrow(() -> new NotFoundException("Argomento non trovato"));
        return items(studentId, practiceExercisesRepository.findVisibleByTopic(topic.getTopicId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PracticeItemDTO> getByIds(long studentId, List<Long> practiceExerciseIds) {
        if (practiceExerciseIds.isEmpty()) {
            return List.of();
        }
        Map<Long, PracticeItemDTO> byId = new HashMap<>();
        items(studentId, practiceExercisesRepository.findAllById(practiceExerciseIds).stream()
                .filter(StudentVisibility::isVisible).toList())
                .forEach(item -> byId.put(item.practiceExerciseId(), item));
        return practiceExerciseIds.stream().map(byId::get).filter(java.util.Objects::nonNull).toList();
    }

    // "Sorprendimi": la skill con il livello più basso tra quelle che hanno voci aperte; a parità, meno punti
    private String weakestSkill(long studentId, Subject subject, List<PracticeItemDTO> open) {
        return skillLevelsService.getLevels(studentId, subject.getSubjectId()).stream()
                .filter(level -> open.stream().anyMatch(item -> item.skillCode().equals(level.code())))
                .min(Comparator.comparingInt(SkillLevelStateDTO::level).thenComparing(SkillLevelStateDTO::points))
                .map(SkillLevelStateDTO::code).orElse(null);
    }

    private static int goalRank(String status) {
        return RETRY.equals(status) ? 0 : NEW.equals(status) ? 1 : 2;
    }

    private List<PracticeItemDTO> items(long studentId, List<PracticeExercise> exercises) {
        if (exercises.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Personal> personal = new HashMap<>();
        for (Object[] row : practiceExercisesRepository.findPersonalStates(studentId)) {
            personal.put(((Number) row[0]).longValue(), new Personal((BigDecimal) row[1], Boolean.TRUE.equals(row[2]),
                    Boolean.TRUE.equals(row[3]), Boolean.TRUE.equals(row[4]),
                    row[5] == null ? null : Instant.ofEpochMilli((long) (((Number) row[5]).doubleValue() * 1000))));
        }
        Map<Long, PracticeLockDTO> locks = skillLevelsService.getLocks(studentId,
                exercises.stream().map(PracticeExercise::getPracticeExerciseId).toList());
        boolean premium = entitlementService.hasPremium(studentId);
        List<PracticeItemDTO> items = new ArrayList<>();
        for (PracticeExercise exercise : exercises) {
            Personal state = personal.getOrDefault(exercise.getPracticeExerciseId(), new Personal(null, false, false, false, null));
            // Sotto la soglia nessun punto; sopra, punti massimi per la percentuale migliore (§10.1)
            BigDecimal points = state.passed()
                    ? exercise.getMaxPoints().multiply(state.best()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2);
            Topic topic = exercise.getTopic();
            items.add(new PracticeItemDTO(exercise.getPracticeExerciseId(), exercise.getQuestionBankEntry().getTitle(),
                    exercise.getSkill().getCodeSkill(), topic.getTopicId(), topic.getTopicName(), topic.getImageReference(),
                    exercise.getEstimatedMinutes() == null ? null : exercise.getEstimatedMinutes().intValue(),
                    exercise.getMaxPoints(), points, state.status(), exercise.getDifficulty(),
                    locks.get(exercise.getPracticeExerciseId()),
                    exercise.getAccessTier() == AccessTier.premium && !premium,
                    state.lastActivity() == null ? null : state.lastActivity().toString()));
        }
        return items;
    }

    private Map<Long, SkillLevelStateDTO> levels(long studentId, long subjectId) {
        Map<Long, SkillLevelStateDTO> levels = new HashMap<>();
        skillLevelsService.getLevels(studentId, subjectId).forEach(level -> levels.put(level.skillId(), level));
        return levels;
    }

    private SubjectCardDTO card(Subject subject) {
        return SubjectCardDTO.from(subject,
                coursesRepository.existsBySubject_SubjectIdAndStatus(subject.getSubjectId(), ContentStatus.published));
    }

    private Subject findSubject(String subjectSlug) {
        return subjectsRepository.findBySlug(subjectSlug)
                .filter(StudentVisibility::isVisible)
                .orElseThrow(() -> new NotFoundException("Materia non trovata"));
    }
}
