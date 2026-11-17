package giuliacrepaldi.BStudent_Management.services.student;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationOption;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationQuestion;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationRecommendation;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationTest;
import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;
import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.StudentTestDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.StudentTestResultDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationOptionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationQuestionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationRecommendationsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationTestsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SubjectSkillsRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.orientation.interfaces.IOrientationService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentPracticeService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentTestsService;
import giuliacrepaldi.BStudent_Management.utilities.TestRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentTestsService implements IStudentTestsService {

    // Voci consigliate dal test di orientamento (blueprint §8.1)
    private static final int MAX_RECOMMENDED = 5;

    private final SubjectsRepository subjectsRepository;
    private final OrientationTestsRepository testsRepository;
    private final OrientationQuestionsRepository questionsRepository;
    private final OrientationOptionsRepository optionsRepository;
    private final OrientationRecommendationsRepository recommendationsRepository;
    private final SubjectSkillsRepository subjectSkillsRepository;
    private final IOrientationService orientationService;
    private final IStudentPracticeService practiceService;
    private final ISkillLevelsService skillLevelsService;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public StudentTestsService(SubjectsRepository subjectsRepository, OrientationTestsRepository testsRepository,
                               OrientationQuestionsRepository questionsRepository,
                               OrientationOptionsRepository optionsRepository,
                               OrientationRecommendationsRepository recommendationsRepository,
                               SubjectSkillsRepository subjectSkillsRepository, IOrientationService orientationService,
                               IStudentPracticeService practiceService, ISkillLevelsService skillLevelsService) {
        this.subjectsRepository = subjectsRepository;
        this.testsRepository = testsRepository;
        this.questionsRepository = questionsRepository;
        this.optionsRepository = optionsRepository;
        this.recommendationsRepository = recommendationsRepository;
        this.subjectSkillsRepository = subjectSkillsRepository;
        this.orientationService = orientationService;
        this.practiceService = practiceService;
        this.skillLevelsService = skillLevelsService;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentTestDTO getTest(String subjectSlug, OrientationTestKind kind) {
        OrientationTest test = findReadyTest(findSubject(subjectSlug), kind);
        if (test == null) {
            return null;
        }
        List<OrientationQuestion> questions = questions(test);
        Map<Long, List<OrientationOption>> options = optionsByQuestion(questions);
        return new StudentTestDTO(test.getOrientationTestId(), test.getKind(), test.getTitle(), questions.stream()
                .map(question -> new StudentTestDTO.Question(question.getOrientationQuestionId(), question.getQuestionText(),
                        options.getOrDefault(question.getOrientationQuestionId(), List.of()).stream()
                                // Senza livelli né esercizi consigliati: lo studente non deve vedere "la risposta giusta"
                                .map(option -> new StudentTestDTO.Option(option.getOrientationOptionId(), option.getOptionText()))
                                .toList()))
                .toList());
    }

    @Override
    @Transactional
    public StudentTestResultDTO submit(long studentId, String subjectSlug, OrientationTestKind kind, Map<Long, Long> answers) {
        Subject subject = findSubject(subjectSlug);
        OrientationTest test = findReadyTest(subject, kind);
        if (test == null) {
            throw new NotFoundException("Il test non è disponibile per questa materia");
        }
        List<OrientationQuestion> questions = questions(test);
        Map<Long, List<OrientationOption>> options = optionsByQuestion(questions);
        // Tutte le domande, un'opzione per domanda, ogni opzione della sua domanda
        List<OrientationOption> chosen = new ArrayList<>();
        Map<String, Long> saved = new LinkedHashMap<>();
        for (OrientationQuestion question : questions) {
            Long optionId = answers.get(question.getOrientationQuestionId());
            if (optionId == null) {
                throw new BadRequestException("Rispondi a tutte le domande prima di inviare il test");
            }
            OrientationOption option = options.getOrDefault(question.getOrientationQuestionId(), List.of()).stream()
                    .filter(o -> o.getOrientationOptionId().equals(optionId)).findFirst()
                    .orElseThrow(() -> new BadRequestException("Una risposta non appartiene alla sua domanda: ricarica la pagina e riprova"));
            chosen.add(option);
            saved.put(String.valueOf(question.getOrientationQuestionId()), optionId);
        }
        if (answers.size() != questions.size()) {
            throw new BadRequestException("Le risposte non corrispondono alle domande del test: ricarica la pagina e riprova");
        }
        String answersJson = jsonMapper.writeValueAsString(saved);
        String now = Instant.now().toString();

        if (kind == OrientationTestKind.course_level) {
            CourseLevel level = TestRules.levelFor(chosen.stream().map(OrientationOption::getLevel).toList());
            testsRepository.saveLevelResult(studentId, test.getOrientationTestId(), answersJson, level.name());
            assignStartingLevels(studentId, subject, level);
            return new StudentTestResultDTO(kind, level, List.of(), now);
        }
        Map<Long, List<TestRules.Recommendation>> byOption = new HashMap<>();
        for (OrientationRecommendation recommendation : recommendationsRepository.findByOptionIds(
                chosen.stream().map(OrientationOption::getOrientationOptionId).toList())) {
            byOption.computeIfAbsent(recommendation.getId().getOrientationOptionId(), id -> new ArrayList<>())
                    .add(new TestRules.Recommendation(recommendation.getId().getPracticeExerciseId(),
                            recommendation.getRecommendationSortOrder()));
        }
        // Solo le voci che lo studente vede oggi: si ordina tutto e si tengono le prime visibili
        List<Long> ordered = TestRules.recommend(new ArrayList<>(byOption.values()), Integer.MAX_VALUE);
        List<PracticeItemDTO> recommended = practiceService.getByIds(studentId, ordered).stream().limit(MAX_RECOMMENDED).toList();
        testsRepository.saveExercisesResult(studentId, test.getOrientationTestId(), answersJson,
                recommended.stream().map(item -> String.valueOf(item.practiceExerciseId())).collect(Collectors.joining(",", "{", "}")));
        return new StudentTestResultDTO(kind, null, recommended, now);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentTestResultDTO getLastResult(long studentId, String subjectSlug, OrientationTestKind kind) {
        OrientationTest test = testsRepository.findBySubject_SubjectIdAndKind(findSubject(subjectSlug).getSubjectId(), kind)
                .orElse(null);
        if (test == null) {
            return null;
        }
        List<Object[]> rows = testsRepository.findLastResult(studentId, test.getOrientationTestId());
        if (rows.isEmpty()) {
            return null;
        }
        Object[] row = rows.get(0);
        if (row[0] != null) {
            return new StudentTestResultDTO(kind, CourseLevel.valueOf((String) row[0]), List.of(), (String) row[2]);
        }
        String ids = (String) row[1];
        List<Long> exerciseIds = ids == null || ids.isBlank() ? List.of()
                : Arrays.stream(ids.split(",")).map(Long::valueOf).toList();
        // La fotografia salvata resta; qui si mostrano solo le voci ancora visibili
        return new StudentTestResultDTO(kind, null, practiceService.getByIds(studentId, exerciseIds), (String) row[2]);
    }

    // Livello di partenza delle skill della materia dal test di livello (Blueprint Gamification §2.6)
    private void assignStartingLevels(long studentId, Subject subject, CourseLevel level) {
        int startingLevel = TestRules.startingLevelFor(level);
        for (SubjectSkill subjectSkill : subjectSkillsRepository.findBySubjectOrdered(subject.getSubjectId())) {
            long skillId = subjectSkill.getId().getSkillId();
            // Una skill con meno livelli riceve il suo massimo; può solo salire
            skillLevelsService.setStartingLevel(studentId, subject.getSubjectId(), skillId,
                    Math.min(startingLevel, skillLevelsService.getMaxLevel(skillId)));
        }
    }

    // Lo studente usa solo i test senza problemi: non esiste uno stato "pubblicato" per i test (§8)
    private OrientationTest findReadyTest(Subject subject, OrientationTestKind kind) {
        return testsRepository.findBySubject_SubjectIdAndKind(subject.getSubjectId(), kind)
                .filter(test -> orientationService.checkReadiness(test.getOrientationTestId()).isEmpty())
                .orElse(null);
    }

    private List<OrientationQuestion> questions(OrientationTest test) {
        return questionsRepository.findByOrientationTest_OrientationTestIdOrderByQuestionSortOrder(test.getOrientationTestId());
    }

    private Map<Long, List<OrientationOption>> optionsByQuestion(List<OrientationQuestion> questions) {
        Map<Long, List<OrientationOption>> options = new HashMap<>();
        if (questions.isEmpty()) {
            return options;
        }
        for (OrientationOption option : optionsRepository.findByQuestionIds(
                questions.stream().map(OrientationQuestion::getOrientationQuestionId).toList())) {
            options.computeIfAbsent(option.getOrientationQuestion().getOrientationQuestionId(), id -> new ArrayList<>()).add(option);
        }
        return options;
    }

    private Subject findSubject(String subjectSlug) {
        return subjectsRepository.findBySlug(subjectSlug)
                .filter(StudentVisibility::isVisible)
                .orElseThrow(() -> new NotFoundException("Materia non trovata"));
    }
}
