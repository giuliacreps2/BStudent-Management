package giuliacrepaldi.BStudent_Management.services.orientation;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationOption;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationQuestion;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationRecommendation;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationTest;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationOptionDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationQuestionDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationTestDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationTestDetailDTO;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationOptionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationQuestionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationRecommendationsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationTestsRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ISubjectsService;
import giuliacrepaldi.BStudent_Management.services.orientation.interfaces.IOrientationService;
import giuliacrepaldi.BStudent_Management.services.practice.interfaces.IPracticeCatalogService;
import giuliacrepaldi.BStudent_Management.utilities.Reorders;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Service
public class OrientationService implements IOrientationService {

    private static final int MIN_QUESTIONS = 3;
    private static final int MIN_OPTIONS = 2;

    private final OrientationTestsRepository testsRepository;
    private final OrientationQuestionsRepository questionsRepository;
    private final OrientationOptionsRepository optionsRepository;
    private final OrientationRecommendationsRepository recommendationsRepository;
    private final ISubjectsService subjectsService;
    private final IPracticeCatalogService practiceCatalogService;
    private final EntityManager entityManager;

    public OrientationService(OrientationTestsRepository testsRepository,
                              OrientationQuestionsRepository questionsRepository,
                              OrientationOptionsRepository optionsRepository,
                              OrientationRecommendationsRepository recommendationsRepository,
                              ISubjectsService subjectsService, IPracticeCatalogService practiceCatalogService,
                              EntityManager entityManager) {
        this.testsRepository = testsRepository;
        this.questionsRepository = questionsRepository;
        this.optionsRepository = optionsRepository;
        this.recommendationsRepository = recommendationsRepository;
        this.subjectsService = subjectsService;
        this.practiceCatalogService = practiceCatalogService;
        this.entityManager = entityManager;
    }

    //TEST

    @Override
    @Transactional
    public OrientationTest saveNewTest(long subjectId, OrientationTestDTO body) {
        Subject subject = subjectsService.findById(subjectId);
        if (testsRepository.existsBySubject_SubjectIdAndKind(subjectId, body.kind())) {
            throw new ConflictException("La materia ha già un " + kindLabel(body.kind()));
        }
        return testsRepository.save(OrientationTest.builder()
                .subject(subject)
                .kind(body.kind())
                .title(body.title().strip())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrientationTest> findBySubject(long subjectId) {
        // 404 se la materia non esiste, invece di una lista vuota
        subjectsService.findById(subjectId);
        return testsRepository.findBySubject_SubjectIdOrderByKind(subjectId);
    }

    @Override
    @Transactional(readOnly = true)
    public OrientationTestDetailDTO getDetail(long testId) {
        return detail(findTest(testId));
    }

    @Override
    @Transactional
    public OrientationTestDetailDTO updateTest(long testId, OrientationTestDTO body) {
        OrientationTest test = findTest(testId);
        // Il body lo ripete, ma le opzioni hanno regole diverse secondo il tipo
        if (test.getKind() != body.kind()) {
            throw new BadRequestException("Il tipo di un test non si cambia: creane uno nuovo");
        }
        test.setTitle(body.title().strip());
        return detail(test);
    }

    @Override
    @Transactional
    public void deleteTest(long testId) {
        OrientationTest test = findTest(testId);
        if (testsRepository.hasResults(testId)) {
            throw new ConflictException("Il test è già stato svolto da qualche studente: non si elimina");
        }
        List<OrientationQuestion> questions = questions(testId);
        deleteQuestionsWithOptions(questions);
        testsRepository.delete(test);
    }

    //DOMANDE

    @Override
    @Transactional
    public OrientationTestDetailDTO addQuestion(long testId, OrientationQuestionDTO body) {
        OrientationTest test = findTest(testId);
        // In coda: due creazioni contemporanee sullo stesso test violano l'unicità -> 409
        questionsRepository.save(OrientationQuestion.builder()
                .orientationTest(test)
                .questionText(body.text().strip())
                .questionSortOrder(questionsRepository.findMaxSortOrder(testId) + 1)
                .build());
        return detail(test);
    }

    @Override
    @Transactional
    public OrientationTestDetailDTO updateQuestion(long questionId, OrientationQuestionDTO body) {
        OrientationQuestion question = findQuestion(questionId);
        // Ammessa anche dopo che il test è stato svolto: i risultati salvati sono una fotografia (§8.3)
        question.setQuestionText(body.text().strip());
        return detail(question.getOrientationTest());
    }

    @Override
    @Transactional
    public OrientationTestDetailDTO reorderQuestions(long testId, List<Long> questionIds) {
        OrientationTest test = findTest(testId);
        List<OrientationQuestion> current = questions(testId);
        Map<Long, OrientationQuestion> byId = current.stream()
                .collect(Collectors.toMap(OrientationQuestion::getOrientationQuestionId, Function.identity()));
        Reorders.validateIds(questionIds, byId.keySet(), "domande del test");
        reorderInTwoSteps(questionIds.stream().map(byId::get).toList(),
                OrientationQuestion::getQuestionSortOrder, OrientationQuestion::setQuestionSortOrder);
        return detail(test);
    }

    @Override
    @Transactional
    public OrientationTestDetailDTO deleteQuestion(long questionId) {
        OrientationQuestion question = findQuestion(questionId);
        OrientationTest test = question.getOrientationTest();
        // Il buco nell'ordine (es. 1, 3) non viola il vincolo: si sistema con il riordino
        deleteQuestionsWithOptions(List.of(question));
        return detail(test);
    }

    //OPZIONI

    @Override
    @Transactional
    public OrientationTestDetailDTO addOption(long questionId, OrientationOptionDTO body) {
        OrientationQuestion question = findQuestion(questionId);
        OrientationTest test = question.getOrientationTest();
        List<OrientationOptionDTO.Recommendation> recommendations = validateOption(test, body);
        OrientationOption option = optionsRepository.save(OrientationOption.builder()
                .orientationQuestion(question)
                .optionText(body.text().strip())
                .optionSortOrder(optionsRepository.findMaxSortOrder(questionId) + 1)
                .level(body.level())
                .build());
        saveRecommendations(option, recommendations);
        return detail(test);
    }

    @Override
    @Transactional
    public OrientationTestDetailDTO updateOption(long optionId, OrientationOptionDTO body) {
        OrientationOption option = findOption(optionId);
        OrientationTest test = option.getOrientationQuestion().getOrientationTest();
        List<OrientationOptionDTO.Recommendation> recommendations = validateOption(test, body);
        // Vale per i prossimi svolgimenti: i risultati salvati non si ricalcolano (§8.3)
        option.setOptionText(body.text().strip());
        option.setLevel(body.level());
        // Sostituzione in blocco: DELETE eseguito subito, poi i nuovi INSERT
        recommendationsRepository.deleteByOptionId(optionId);
        saveRecommendations(option, recommendations);
        return detail(test);
    }

    @Override
    @Transactional
    public OrientationTestDetailDTO reorderOptions(long questionId, List<Long> optionIds) {
        OrientationQuestion question = findQuestion(questionId);
        List<OrientationOption> current = optionsRepository.findByOrientationQuestion_OrientationQuestionIdOrderByOptionSortOrder(questionId);
        Map<Long, OrientationOption> byId = current.stream()
                .collect(Collectors.toMap(OrientationOption::getOrientationOptionId, Function.identity()));
        Reorders.validateIds(optionIds, byId.keySet(), "opzioni della domanda");
        reorderInTwoSteps(optionIds.stream().map(byId::get).toList(),
                OrientationOption::getOptionSortOrder, OrientationOption::setOptionSortOrder);
        return detail(question.getOrientationTest());
    }

    @Override
    @Transactional
    public OrientationTestDetailDTO deleteOption(long optionId) {
        OrientationOption option = findOption(optionId);
        OrientationTest test = option.getOrientationQuestion().getOrientationTest();
        if (testsRepository.isAnyOptionChosen(List.of(optionId))) {
            throw new ConflictException("Questa risposta è già stata scelta da qualche studente: non si elimina, ma puoi modificarne il testo");
        }
        recommendationsRepository.deleteByOptionId(optionId);
        optionsRepository.delete(option);
        return detail(test);
    }

    //PRONTO

    @Override
    @Transactional(readOnly = true)
    public List<String> checkReadiness(long testId) {
        return detail(findTest(testId)).problems();
    }

    //SUPPORTO

    // Regole del tipo di test sull'opzione; restituisce i consigli già validati (vuoti se null)
    private List<OrientationOptionDTO.Recommendation> validateOption(OrientationTest test, OrientationOptionDTO body) {
        List<OrientationOptionDTO.Recommendation> recommendations =
                body.recommendations() == null ? List.of() : body.recommendations();
        if (test.getKind() == OrientationTestKind.course_level) {
            if (body.level() == null) {
                throw new BadRequestException("Nel test di livello ogni risposta indica un livello");
            }
            if (!recommendations.isEmpty()) {
                throw new BadRequestException("Nel test di livello le risposte non consigliano esercizi");
            }
            return List.of();
        }
        if (body.level() != null) {
            throw new BadRequestException("Nel test di orientamento le risposte consigliano esercizi, non livelli");
        }
        Set<Long> seen = new HashSet<>();
        Long subjectId = test.getSubject().getSubjectId();
        for (OrientationOptionDTO.Recommendation recommendation : recommendations) {
            if (!seen.add(recommendation.practiceExerciseId())) {
                throw new BadRequestException("Lo stesso esercizio è consigliato più volte dalla stessa risposta");
            }
            PracticeExercise exercise = practiceCatalogService.findById(recommendation.practiceExerciseId());
            if (!exercise.getTopic().getSubject().getSubjectId().equals(subjectId)) {
                throw new BadRequestException("L'esercizio «" + exercise.getQuestionBankEntry().getInternalQuestionTitle()
                        + "» è di un'altra materia");
            }
        }
        return recommendations;
    }

    private void saveRecommendations(OrientationOption option, List<OrientationOptionDTO.Recommendation> recommendations) {
        for (OrientationOptionDTO.Recommendation recommendation : recommendations) {
            // persist e non save: con la chiave composta già valorizzata save farebbe un merge
            entityManager.persist(OrientationRecommendation.builder()
                    .id(new OrientationRecommendation.OrientationRecommendationId(
                            option.getOrientationOptionId(), recommendation.practiceExerciseId()))
                    .orientationOption(option)
                    .practiceExercise(practiceCatalogService.findById(recommendation.practiceExerciseId()))
                    .recommendationSortOrder(recommendation.priority())
                    .build());
        }
        entityManager.flush();
    }

    private void deleteQuestionsWithOptions(List<OrientationQuestion> questions) {
        if (questions.isEmpty()) {
            return;
        }
        List<Long> questionIds = questions.stream().map(OrientationQuestion::getOrientationQuestionId).toList();
        List<OrientationOption> options = optionsRepository.findByQuestionIds(questionIds);
        List<Long> optionIds = options.stream().map(OrientationOption::getOrientationOptionId).toList();
        if (!optionIds.isEmpty()) {
            if (testsRepository.isAnyOptionChosen(optionIds)) {
                throw new ConflictException("Alcune risposte sono già state scelte da qualche studente: la domanda non si elimina, ma puoi modificarne il testo");
            }
            recommendationsRepository.deleteByOptionIds(optionIds);
            optionsRepository.deleteAll(options);
        }
        questionsRepository.deleteAll(questions);
        entityManager.flush();
    }

    // Gli indici unici (contenitore, sort_order) dei test non sono DEFERRABLE: prima tutti sopra il massimo
    // attuale, poi le posizioni finali 1..n. Il CHECK sort_order > 0 resta sempre rispettato
    private <T> void reorderInTwoSteps(List<T> ordered, ToIntFunction<T> getter, ObjIntSetter<T> setter) {
        int max = ordered.stream().mapToInt(getter).max().orElse(0);
        for (int i = 0; i < ordered.size(); i++) {
            setter.set(ordered.get(i), max + i + 1);
        }
        entityManager.flush();
        for (int i = 0; i < ordered.size(); i++) {
            setter.set(ordered.get(i), i + 1);
        }
        entityManager.flush();
    }

    @FunctionalInterface
    private interface ObjIntSetter<T> {
        void set(T target, int value);
    }

    private OrientationTestDetailDTO detail(OrientationTest test) {
        entityManager.flush();
        List<OrientationQuestion> questions = questions(test.getOrientationTestId());
        List<Long> questionIds = questions.stream().map(OrientationQuestion::getOrientationQuestionId).toList();
        List<OrientationOption> options = questionIds.isEmpty() ? List.of() : optionsRepository.findByQuestionIds(questionIds);
        List<Long> optionIds = options.stream().map(OrientationOption::getOrientationOptionId).toList();
        Map<Long, List<OrientationRecommendation>> recommendationsByOption = optionIds.isEmpty() ? Map.of()
                : recommendationsRepository.findByOptionIds(optionIds).stream()
                        .collect(Collectors.groupingBy(r -> r.getId().getOrientationOptionId()));
        Map<Long, List<OrientationOption>> optionsByQuestion = options.stream()
                .collect(Collectors.groupingBy(o -> o.getOrientationQuestion().getOrientationQuestionId()));

        List<String> problems = new ArrayList<>();
        if (questions.size() < MIN_QUESTIONS) {
            problems.add("servono almeno " + MIN_QUESTIONS + " domande (ora " + questions.size() + ")");
        }
        List<OrientationTestDetailDTO.Question> questionViews = new ArrayList<>();
        for (OrientationQuestion question : questions) {
            List<OrientationOption> questionOptions = optionsByQuestion.getOrDefault(question.getOrientationQuestionId(), List.of());
            if (questionOptions.size() < MIN_OPTIONS) {
                problems.add("la domanda «" + question.getQuestionText() + "» ha bisogno di almeno " + MIN_OPTIONS + " risposte");
            }
            List<OrientationTestDetailDTO.Option> optionViews = new ArrayList<>();
            for (OrientationOption option : questionOptions) {
                if (test.getKind() == OrientationTestKind.course_level && option.getLevel() == null) {
                    problems.add("la risposta «" + option.getOptionText() + "» non indica un livello");
                }
                List<OrientationTestDetailDTO.Recommendation> recommendationViews =
                        recommendationsByOption.getOrDefault(option.getOrientationOptionId(), List.of()).stream()
                                .map(r -> new OrientationTestDetailDTO.Recommendation(
                                        r.getPracticeExercise().getPracticeExerciseId(),
                                        r.getPracticeExercise().getQuestionBankEntry().getTitle(),
                                        r.getRecommendationSortOrder()))
                                .toList();
                optionViews.add(new OrientationTestDetailDTO.Option(option.getOrientationOptionId(),
                        option.getOptionText(), option.getOptionSortOrder(), option.getLevel(), recommendationViews));
            }
            questionViews.add(new OrientationTestDetailDTO.Question(question.getOrientationQuestionId(),
                    question.getQuestionText(), question.getQuestionSortOrder(), optionViews));
        }
        return new OrientationTestDetailDTO(test.getOrientationTestId(), test.getSubject().getSubjectId(),
                test.getKind(), test.getTitle(), questionViews, problems);
    }

    private List<OrientationQuestion> questions(long testId) {
        return questionsRepository.findByOrientationTest_OrientationTestIdOrderByQuestionSortOrder(testId);
    }

    private OrientationTest findTest(long testId) {
        return testsRepository.findById(testId).orElseThrow(() -> new NotFoundException(testId));
    }

    private OrientationQuestion findQuestion(long questionId) {
        return questionsRepository.findById(questionId).orElseThrow(() -> new NotFoundException(questionId));
    }

    private OrientationOption findOption(long optionId) {
        return optionsRepository.findById(optionId).orElseThrow(() -> new NotFoundException(optionId));
    }

    private static String kindLabel(OrientationTestKind kind) {
        return kind == OrientationTestKind.course_level ? "test di livello" : "test di orientamento";
    }
}
