package giuliacrepaldi.BStudent_Management.services.assessment;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.exercises.Question;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionAnswer;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.exercises.KindQuestion;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.ForbiddenException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionStepOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionAssessedDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionListItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.TeacherAssessmentDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionAnswersRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionRubricCriteriaRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.UsersRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.services.assessment.interfaces.IAssessmentsService;
import giuliacrepaldi.BStudent_Management.services.assessment.interfaces.ISubmissionsService;
import giuliacrepaldi.BStudent_Management.services.attempts.interfaces.IMissionRunsService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IPracticeProgressService;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class SubmissionsService implements ISubmissionsService {

    // Chi può correggere: oggi solo l'Admin (Svolgimento §4.2)
    private static final Set<String> CORRECTOR_ROLES = Set.of("ADMIN");
    private static final int MAX_PAGE_SIZE = 100;

    private final ExerciseAttemptsRepository attemptsRepository;
    private final ExerciseAssessmentsRepository assessmentsRepository;
    private final QuestionsRepository questionsRepository;
    private final QuestionAnswersRepository answersRepository;
    private final QuestionRubricCriteriaRepository rubricRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;
    private final UsersRepository usersRepository;
    private final IAssessmentsService assessmentsService;
    private final IPracticeProgressService practiceProgressService;
    private final IMissionRunsService missionRunsService;

    public SubmissionsService(ExerciseAttemptsRepository attemptsRepository,
                              ExerciseAssessmentsRepository assessmentsRepository, QuestionsRepository questionsRepository,
                              QuestionAnswersRepository answersRepository, QuestionRubricCriteriaRepository rubricRepository,
                              PracticeExercisesRepository practiceExercisesRepository, UsersRepository usersRepository,
                              IAssessmentsService assessmentsService, IPracticeProgressService practiceProgressService,
                              IMissionRunsService missionRunsService) {
        this.attemptsRepository = attemptsRepository;
        this.assessmentsRepository = assessmentsRepository;
        this.questionsRepository = questionsRepository;
        this.answersRepository = answersRepository;
        this.rubricRepository = rubricRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
        this.usersRepository = usersRepository;
        this.assessmentsService = assessmentsService;
        this.practiceProgressService = practiceProgressService;
        this.missionRunsService = missionRunsService;
    }

    @Override
    public boolean canAssess(User user) {
        return user != null && user.getRoles().stream().anyMatch(role -> CORRECTOR_ROLES.contains(role.getCode()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubmissionListItemDTO> findPending(User corrector, Long subjectId, int page, int size) {
        requireCorrector(corrector);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        return attemptsRepository.findPending(subjectId, pageable).map(attempt -> new SubmissionListItemDTO(
                attempt.getExerciseAttemptId(), attempt.getContext(), attempt.getStudent().getUsername(),
                subjectName(attempt), place(attempt), attempt.getQuestionBankEntry().getInternalQuestionTitle(),
                question(attempt).getKindQuestion(), attempt.getSubmittedAt()));
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionDetailDTO getSubmission(User corrector, long exerciseAttemptId) {
        requireCorrector(corrector);
        ExerciseAttempt attempt = attemptsRepository.findById(exerciseAttemptId)
                .orElseThrow(() -> new NotFoundException("Consegna non trovata"));
        ExerciseAssessment assessment = assessmentsRepository.findById(exerciseAttemptId)
                .filter(a -> a.getAssessmentMethod() == AssessmentMethod.teacher).orElse(null);
        if (assessment == null) {
            requirePending(attempt);
        }
        Question question = question(attempt);
        List<QuestionAnswer> answers = answersRepository.findByQuestion_QuestionIdOrderByAnswerSortOrder(question.getQuestionId());
        // Soluzioni dell'autrice: per la scelta multipla solo quella giusta, per gli altri tipi tutte le accettate
        List<String> reference = (question.getKindQuestion() == KindQuestion.multiple_choice
                ? answers.stream().filter(QuestionAnswer::isCorrect) : answers.stream())
                .map(QuestionAnswer::getAnswerText).toList();
        List<SubmissionDetailDTO.RubricCriterion> rubric = rubricRepository
                .findByQuestion_QuestionIdOrderByCriterionSortOrder(question.getQuestionId()).stream()
                .map(c -> new SubmissionDetailDTO.RubricCriterion(c.getCategory(), c.getDescription())).toList();
        MissionAttempt missionAttempt = attempt.getMissionAttempt();
        return new SubmissionDetailDTO(attempt.getExerciseAttemptId(), attempt.getContext(), attempt.getStatus(),
                attempt.getStudent().getUsername(), subjectName(attempt), place(attempt),
                missionAttempt != null ? missionAttempt.getAttemptNumber() : null,
                attempt.getQuestionBankEntry().getInternalQuestionTitle(), assessmentsService.toView(attempt),
                answerText(attempt, answers), reference, blankToNull(question.getGeneralExplanation()), rubric,
                maxPoints(attempt), assessment != null ? assessment.getPassingThreshold() : assessmentsService.passingThreshold(),
                attempt.getSubmittedAt(),
                assessment == null ? null : new SubmissionDetailDTO.Assessment(assessment.getPercentage(),
                        Boolean.TRUE.equals(assessment.getPassed()), assessment.getFeedback(), assessment.getValidatedAt()));
    }

    @Override
    @Transactional
    public SubmissionAssessedDTO assess(User corrector, long exerciseAttemptId, TeacherAssessmentDTO body) {
        requireCorrector(corrector);
        // Prima lettura e riga bloccata: lo studente non può annullare l'invio mentre si salva l'esito
        ExerciseAttempt attempt = attemptsRepository.lockById(exerciseAttemptId)
                .orElseThrow(() -> new NotFoundException("Consegna non trovata"));
        if (attempt.getStatus() == ExerciseAttemptStatus.assessed || assessmentsRepository.existsById(exerciseAttemptId)) {
            throw new ConflictException("Questa consegna è già stata corretta: il giudizio è definitivo");
        }
        requirePending(attempt);
        ExerciseAssessment assessment = assessmentsService.assessByTeacher(attempt,
                usersRepository.getReferenceById(corrector.getUserId()), body.percentage(), body.feedback().strip());
        boolean passed = Boolean.TRUE.equals(assessment.getPassed());
        String effect;
        if (attempt.getContext() == ExerciseContext.practice) {
            practiceProgressService.onAssessed(attempt.getStudent().getStudentId(), exerciseAttemptId);
            effect = passed
                    ? "Esercizio superato: lo studente riceve i punti e vede il tuo commento"
                    : "Esercizio non superato: lo studente vede il tuo commento e la soluzione";
        } else {
            MissionStepOutcomeDTO outcome = missionRunsService.onTeacherAssessed(exerciseAttemptId);
            effect = switch (outcome.result()) {
                case "mission_passed" -> "Missione superata: lo studente riceve punti ed esperienza";
                case "passed" -> "Esercizio superato: lo studente può proseguire la missione";
                case "failed" -> "Esercizio non superato: lo studente perde una vita e rifà solo questo esercizio";
                default -> "Correzione salvata";
            };
        }
        return new SubmissionAssessedDTO(exerciseAttemptId, assessment.getPercentage(), assessment.getPassingThreshold(),
                passed, assessment.getFeedback(), effect);
    }

    // ------------------------------------------------------------------ Supporto

    private void requireCorrector(User user) {
        if (!canAssess(user)) {
            throw new ForbiddenException("Non hai i permessi per correggere le consegne");
        }
    }

    private static void requirePending(ExerciseAttempt attempt) {
        if (attempt.getStatus() != ExerciseAttemptStatus.pending_review) {
            if (attempt.getStatus() == ExerciseAttemptStatus.active || attempt.getStatus() == ExerciseAttemptStatus.saved) {
                throw new ConflictException("Lo studente ha annullato l'invio per modificare la risposta: "
                        + "la consegna tornerà nell'elenco quando la invierà di nuovo");
            }
            throw new ConflictException("Questa consegna non è più da correggere");
        }
    }

    private String subjectName(ExerciseAttempt attempt) {
        if (attempt.getContext() == ExerciseContext.mission) {
            return attempt.getMissionAttempt().getMissionRun().getSubject().getSubjectName();
        }
        return practiceExercise(attempt).getTopic().getSubject().getSubjectName();
    }

    private String place(ExerciseAttempt attempt) {
        if (attempt.getContext() == ExerciseContext.practice) {
            return "Palestra";
        }
        Mission mission = attempt.getMissionAttempt().getMissionRun().getMission();
        return "Missione «" + mission.getMissionTitle() + "» · " + mission.getCourse().getCourseTitle();
    }

    // Punti massimi del momento in cui il tentativo è partito, altrimenti quelli attuali
    private BigDecimal maxPoints(ExerciseAttempt attempt) {
        BigDecimal snapshot = AttemptJson.maxPoints(attempt.getConfigurationSnapshot());
        if (snapshot != null) {
            return snapshot;
        }
        return attempt.getContext() == ExerciseContext.mission
                ? attempt.getMissionExercise().getMaxPoints()
                : practiceExercise(attempt).getMaxPoints();
    }

    private PracticeExercise practiceExercise(ExerciseAttempt attempt) {
        return practiceExercisesRepository
                .findByQuestionBankEntry_QuestionBankEntryId(attempt.getQuestionBankEntry().getQuestionBankEntryId())
                .orElseThrow(() -> new NotFoundException("Voce della Palestra non trovata"));
    }

    private Question question(ExerciseAttempt attempt) {
        return questionsRepository.findByQuestionVersion_QuestionVersionId(attempt.getQuestionVersion().getQuestionVersionId())
                .orElseThrow(() -> new NotFoundException("Domanda dell'esercizio non trovata"));
    }

    // La risposta dello studente come testo: per la scelta multipla il testo dell'opzione scelta
    private static String answerText(ExerciseAttempt attempt, List<QuestionAnswer> answers) {
        AnswerDTO answer = AttemptJson.readAnswer(attempt.getResponse());
        if (answer.selectedAnswerId() != null) {
            return answers.stream().filter(a -> a.getQuestionAnswerId().equals(answer.selectedAnswerId()))
                    .map(QuestionAnswer::getAnswerText).findFirst().orElse(null);
        }
        return answer.text();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
