package giuliacrepaldi.BStudent_Management.services.assessment;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.catalog.AcademySetting;
import giuliacrepaldi.BStudent_Management.entities.exercises.Question;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionAnswer;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.exercises.KindQuestion;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseViewDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.AcademySettingsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionAnswersRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.assessment.interfaces.IAssessmentsService;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AssessmentsService implements IAssessmentsService {

    // Riga unica: il DB impone academy_settings_id = 1
    private static final short ACADEMY_SETTINGS_ID = 1;

    private final ExerciseAssessmentsRepository assessmentsRepository;
    private final QuestionsRepository questionsRepository;
    private final QuestionAnswersRepository answersRepository;
    private final AcademySettingsRepository academySettingsRepository;
    private final ResourcesRepository resourcesRepository;

    public AssessmentsService(ExerciseAssessmentsRepository assessmentsRepository, QuestionsRepository questionsRepository,
                              QuestionAnswersRepository answersRepository,
                              AcademySettingsRepository academySettingsRepository, ResourcesRepository resourcesRepository) {
        this.assessmentsRepository = assessmentsRepository;
        this.questionsRepository = questionsRepository;
        this.answersRepository = answersRepository;
        this.academySettingsRepository = academySettingsRepository;
        this.resourcesRepository = resourcesRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseViewDTO toView(ExerciseAttempt attempt) {
        Question question = question(attempt);
        List<ExerciseViewDTO.Option> options = new ArrayList<>();
        if (question.getKindQuestion() == KindQuestion.multiple_choice) {
            List<QuestionAnswer> answers = answers(question);
            // Nell'ordine salvato nel tentativo; le opzioni non previste dall'ordine vanno in coda
            List<Long> order = AttemptJson.answerOrder(attempt.getConfigurationSnapshot());
            answers.stream()
                    .sorted((a, b) -> Integer.compare(position(order, a), position(order, b)))
                    .forEach(answer -> options.add(new ExerciseViewDTO.Option(answer.getQuestionAnswerId(), answer.getAnswerText())));
        }
        return new ExerciseViewDTO(attempt.getQuestionBankEntry().getTitle(), question.getKindQuestion(), question.getPrompt(),
                question.getSourceText(), question.getAssessmentMode(), options);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> newAnswerOrder(long questionVersionId) {
        Question question = questionsRepository.findByQuestionVersion_QuestionVersionId(questionVersionId)
                .orElseThrow(() -> new NotFoundException("Domanda dell'esercizio non trovata"));
        if (question.getKindQuestion() != KindQuestion.multiple_choice) {
            return List.of();
        }
        // Opzioni mescolate nei nuovi tentativi (§6.2)
        List<Long> order = new ArrayList<>(answers(question).stream().map(QuestionAnswer::getQuestionAnswerId).toList());
        Collections.shuffle(order);
        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAutomatic(ExerciseAttempt attempt) {
        return question(attempt).getAssessmentMode() == AssessmentMethod.automatic;
    }

    @Override
    @Transactional(readOnly = true)
    public void validateAnswer(ExerciseAttempt attempt, AnswerDTO answer, boolean required) {
        Question question = question(attempt);
        if (question.getKindQuestion() == KindQuestion.multiple_choice) {
            if (answer.text() != null && !answer.text().isBlank()) {
                throw new BadRequestException("Questo esercizio chiede di scegliere una risposta, non di scriverla");
            }
            if (answer.selectedAnswerId() == null) {
                if (required) {
                    throw new BadRequestException("Scegli una risposta prima di consegnare");
                }
                return;
            }
            boolean ofQuestion = answers(question).stream()
                    .anyMatch(a -> a.getQuestionAnswerId().equals(answer.selectedAnswerId()));
            if (!ofQuestion) {
                throw new BadRequestException("La risposta scelta non è tra quelle dell'esercizio: ricarica la pagina e riprova");
            }
            return;
        }
        if (answer.selectedAnswerId() != null) {
            throw new BadRequestException("Questo esercizio chiede di scrivere la risposta");
        }
        if (required && (answer.text() == null || answer.text().isBlank())) {
            throw new BadRequestException("Scrivi la risposta prima di consegnare");
        }
    }

    @Override
    @Transactional
    public ExerciseAssessment assessAutomatically(ExerciseAttempt attempt) {
        // Un giudizio per tentativo: la chiave del giudizio è il tentativo
        ExerciseAssessment existing = assessmentsRepository.findById(attempt.getExerciseAttemptId()).orElse(null);
        if (existing != null) {
            return existing;
        }
        Question question = question(attempt);
        if (question.getAssessmentMode() != AssessmentMethod.automatic) {
            throw new IllegalStateException("L'esercizio è corretto dalla docente");
        }
        List<QuestionAnswer> answers = answers(question);
        AnswerDTO response = AttemptJson.readAnswer(attempt.getResponse());
        BigDecimal percentage;
        String feedback = null;
        if (question.getKindQuestion() == KindQuestion.multiple_choice) {
            Long correctId = answers.stream().filter(QuestionAnswer::isCorrect)
                    .map(QuestionAnswer::getQuestionAnswerId).findFirst().orElse(null);
            percentage = PlayRules.gradeChoice(response.selectedAnswerId(), correctId);
            // Feedback dell'opzione scelta, se l'autrice l'ha scritto
            feedback = answers.stream()
                    .filter(a -> a.getQuestionAnswerId().equals(response.selectedAnswerId()))
                    .map(QuestionAnswer::getFeedback).filter(f -> f != null && !f.isBlank()).findFirst().orElse(null);
        } else {
            // Completamento e traduzione di frase: ogni risposta dell'autrice è una soluzione accettata
            percentage = PlayRules.gradeText(response.text(), answers.stream().map(QuestionAnswer::getAnswerText).toList());
        }
        BigDecimal threshold = passingThreshold();
        boolean passed = PlayRules.isPassed(percentage, threshold);
        OffsetDateTime now = OffsetDateTime.now();
        attempt.setStatus(ExerciseAttemptStatus.assessed);
        attempt.setCompletedAt(now);
        attempt.setTimerStartedAt(null);
        if (attempt.getSubmittedAt() == null) {
            attempt.setSubmittedAt(now);
        }
        return assessmentsRepository.save(ExerciseAssessment.builder()
                .exerciseAttempt(attempt)
                .assessmentMethod(AssessmentMethod.automatic)
                .percentage(percentage)
                .passingThreshold(threshold)
                .passed(passed)
                // Il giudizio ha sempre un feedback: quello dell'autrice, oppure una frase neutra
                .feedback(feedback != null ? feedback : passed ? "Risposta corretta." : "Risposta non corretta.")
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public ExerciseOutcomeDTO toOutcome(ExerciseAttempt attempt, ExerciseAssessment assessment) {
        Question question = question(attempt);
        boolean passed = PlayRules.isPassed(assessment.getPercentage(), assessment.getPassingThreshold());
        // La soluzione si vede se l'esercizio è superato, oppure sempre in Palestra; mai dopo un errore in
        // missione, dove lo studente rifà lo stesso esercizio (Svolgimento §3)
        boolean showSolution = passed || attempt.getContext() == ExerciseContext.practice;
        List<String> correct = List.of();
        String explanation = null;
        if (showSolution) {
            List<QuestionAnswer> answers = answers(question);
            correct = (question.getKindQuestion() == KindQuestion.multiple_choice
                    ? answers.stream().filter(QuestionAnswer::isCorrect) : answers.stream())
                    .map(QuestionAnswer::getAnswerText).toList();
            explanation = blankToNull(question.getGeneralExplanation());
        }
        // Dopo un errore: la teoria da rileggere, cioè l'argomento dell'esercizio e la sua Risorsa
        String topicName = null;
        String subjectSlug = null;
        String resourceSlug = null;
        Topic topic = attempt.getQuestionBankEntry().getTopic();
        if (!passed && topic != null) {
            topicName = topic.getTopicName();
            subjectSlug = topic.getSubject().getSlug();
            resourceSlug = resourcesRepository
                    .findFirstByTopic_TopicIdAndStatusOrderByResourceId(topic.getTopicId(), ContentStatus.published)
                    .map(Resource::getSlug).orElse(null);
        }
        return new ExerciseOutcomeDTO(assessment.getPercentage(), assessment.getPassingThreshold(), passed,
                assessment.getFeedback(), correct, explanation, topicName, subjectSlug, resourceSlug, PlayRules.ZERO, null);
    }

    private Question question(ExerciseAttempt attempt) {
        return questionsRepository.findByQuestionVersion_QuestionVersionId(attempt.getQuestionVersion().getQuestionVersionId())
                .orElseThrow(() -> new NotFoundException("Domanda dell'esercizio non trovata"));
    }

    private List<QuestionAnswer> answers(Question question) {
        return answersRepository.findByQuestion_QuestionIdOrderByAnswerSortOrder(question.getQuestionId());
    }

    private BigDecimal passingThreshold() {
        return academySettingsRepository.findById(ACADEMY_SETTINGS_ID)
                .map(AcademySetting::getPassingThreshold)
                .orElseThrow(() -> new NotFoundException("Impostazioni dell'Academy non trovate"));
    }

    private static int position(List<Long> order, QuestionAnswer answer) {
        int index = order.indexOf(answer.getQuestionAnswerId());
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
