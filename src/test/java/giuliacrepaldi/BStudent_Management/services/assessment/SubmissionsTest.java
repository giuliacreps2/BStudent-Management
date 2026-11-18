package giuliacrepaldi.BStudent_Management.services.assessment;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.catalog.AcademySetting;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.identity.Role;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.ForbiddenException;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionStepOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.SubmissionAssessedDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.TeacherAssessmentDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.AcademySettingsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionAnswersRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionRubricCriteriaRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.UsersRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.attempts.interfaces.IMissionRunsService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IPracticeProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// "Consegne da correggere" (Blueprint Svolgimento §1.5, §2, §4)
class SubmissionsTest {

    private static final long STUDENT = 7L;
    private static final long ATTEMPT = 50L;
    private static final long ADMIN_ID = 1L;

    private ExerciseAttemptsRepository attempts;
    private ExerciseAssessmentsRepository assessments;
    private IPracticeProgressService practiceProgress;
    private IMissionRunsService missionRuns;
    private SubmissionsService service;
    private User admin;

    @BeforeEach
    void setUp() {
        attempts = mock(ExerciseAttemptsRepository.class);
        assessments = mock(ExerciseAssessmentsRepository.class);
        when(assessments.save(any(ExerciseAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        practiceProgress = mock(IPracticeProgressService.class);
        missionRuns = mock(IMissionRunsService.class);
        AcademySettingsRepository settings = mock(AcademySettingsRepository.class);
        AcademySetting setting = new AcademySetting();
        setting.setPassingThreshold(new BigDecimal("60.00"));
        when(settings.findById(any())).thenReturn(Optional.of(setting));
        UsersRepository users = mock(UsersRepository.class);
        when(users.getReferenceById(anyLong())).thenAnswer(invocation -> {
            User reference = new User();
            reference.setUserId(invocation.getArgument(0));
            return reference;
        });
        // Giudizio vero: soglia e superamento calcolati da AssessmentsService
        AssessmentsService assessmentsService = new AssessmentsService(assessments, mock(QuestionsRepository.class),
                mock(QuestionAnswersRepository.class), settings, mock(ResourcesRepository.class));
        service = new SubmissionsService(attempts, assessments, mock(QuestionsRepository.class),
                mock(QuestionAnswersRepository.class), mock(QuestionRubricCriteriaRepository.class),
                mock(PracticeExercisesRepository.class), users, assessmentsService, practiceProgress, missionRuns);
        admin = user(ADMIN_ID, "ADMIN");
    }

    private static User user(long id, String roleCode) {
        User user = new User();
        user.setUserId(id);
        Role role = new Role();
        role.setCode(roleCode);
        user.setRoles(Set.of(role));
        return user;
    }

    private ExerciseAttempt attempt(ExerciseContext context, ExerciseAttemptStatus status) {
        Student student = new Student();
        student.setStudentId(STUDENT);
        QuestionBankEntry entry = new QuestionBankEntry();
        entry.setQuestionBankEntryId(40L);
        ExerciseAttempt attempt = ExerciseAttempt.builder().exerciseAttemptId(ATTEMPT).student(student)
                .context(context).questionBankEntry(entry).status(status).configurationSnapshot("{}").build();
        when(attempts.lockById(ATTEMPT)).thenReturn(Optional.of(attempt));
        when(attempts.findById(ATTEMPT)).thenReturn(Optional.of(attempt));
        return attempt;
    }

    private static TeacherAssessmentDTO grade(String percentage) {
        return new TeacherAssessmentDTO(new BigDecimal(percentage), "  Buona traduzione, attenzione ai casi.  ");
    }

    private static MissionStepOutcomeDTO missionResult(String result) {
        return new MissionStepOutcomeDTO(result, null, "failed".equals(result), null, null, List.of(), null);
    }

    @Test
    void soloLAdminPuoCorreggere() {
        User student = user(9L, "STUDENT");

        assertThat(service.canAssess(admin)).isTrue();
        assertThat(service.canAssess(student)).isFalse();
        assertThat(service.canAssess(null)).isFalse();
        assertThatThrownBy(() -> service.assess(student, ATTEMPT, grade("80")))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.findPending(student, null, 0, 20))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void inPalestraIlGiudizioDellaDocenteChiudeIlTentativoEDaIPunti() {
        ExerciseAttempt attempt = attempt(ExerciseContext.practice, ExerciseAttemptStatus.pending_review);

        SubmissionAssessedDTO result = service.assess(admin, ATTEMPT, grade("75"));

        assertThat(result.passed()).isTrue();
        assertThat(result.feedback()).isEqualTo("Buona traduzione, attenzione ai casi.");
        assertThat(result.effect()).contains("superato");
        assertThat(attempt.getStatus()).isEqualTo(ExerciseAttemptStatus.assessed);
        assertThat(attempt.getCompletedAt()).isNotNull();
        verify(assessments).save(any(ExerciseAssessment.class));
        verify(practiceProgress).onAssessed(STUDENT, ATTEMPT);
        verifyNoInteractions(missionRuns);
    }

    @Test
    void ilGiudizioConservaLaDocenteELaSogliaESottoIl60NonESuperato() {
        attempt(ExerciseContext.practice, ExerciseAttemptStatus.pending_review);
        when(assessments.save(any(ExerciseAssessment.class))).thenAnswer(invocation -> {
            ExerciseAssessment saved = invocation.getArgument(0);
            assertThat(saved.getAssessmentMethod()).isEqualTo(AssessmentMethod.teacher);
            assertThat(saved.getTeacher().getUserId()).isEqualTo(ADMIN_ID);
            assertThat(saved.getPassingThreshold()).isEqualByComparingTo("60");
            return saved;
        });

        SubmissionAssessedDTO result = service.assess(admin, ATTEMPT, grade("59.99"));

        assertThat(result.passed()).isFalse();
        assertThat(result.effect()).contains("soluzione");
    }

    @Test
    void inMissioneLEsitoPassaAlloSvolgimentoEDiceCheCosaSuccede() {
        attempt(ExerciseContext.mission, ExerciseAttemptStatus.pending_review);
        when(missionRuns.onTeacherAssessed(ATTEMPT)).thenReturn(missionResult("failed"));

        SubmissionAssessedDTO failed = service.assess(admin, ATTEMPT, grade("40"));

        assertThat(failed.passed()).isFalse();
        assertThat(failed.effect()).contains("perde una vita");
        verify(missionRuns).onTeacherAssessed(ATTEMPT);
        verify(practiceProgress, never()).onAssessed(anyLong(), anyLong());
    }

    @Test
    void missioneSuperataConLUltimoEsercizio() {
        attempt(ExerciseContext.mission, ExerciseAttemptStatus.pending_review);
        when(missionRuns.onTeacherAssessed(ATTEMPT)).thenReturn(missionResult("mission_passed"));

        assertThat(service.assess(admin, ATTEMPT, grade("90")).effect()).startsWith("Missione superata");
    }

    @Test
    void unaConsegnaGiaCorrettaNonSiCorreggeDiNuovo() {
        attempt(ExerciseContext.practice, ExerciseAttemptStatus.pending_review);
        when(assessments.existsById(ATTEMPT)).thenReturn(true);

        assertThatThrownBy(() -> service.assess(admin, ATTEMPT, grade("80")))
                .isInstanceOf(ConflictException.class).hasMessageContaining("definitivo");
        verify(assessments, never()).save(any());
    }

    @Test
    void seLoStudenteHaAnnullatoLInvioLaConsegnaNonSiCorreggeNeSiApre() {
        attempt(ExerciseContext.mission, ExerciseAttemptStatus.saved);

        assertThatThrownBy(() -> service.assess(admin, ATTEMPT, grade("80")))
                .isInstanceOf(ConflictException.class).hasMessageContaining("annullato l'invio");
        assertThatThrownBy(() -> service.getSubmission(admin, ATTEMPT))
                .isInstanceOf(ConflictException.class).hasMessageContaining("tornerà nell'elenco");
        verifyNoInteractions(missionRuns);
    }
}
