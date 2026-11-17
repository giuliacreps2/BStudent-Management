package giuliacrepaldi.BStudent_Management.services.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.progression.PracticeRecord;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import giuliacrepaldi.BStudent_Management.enums.progression.SourceKind;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.ForbiddenException;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseViewDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.PracticeAttemptDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeLockDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionVersionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.PracticeRecordsRepository;
import giuliacrepaldi.BStudent_Management.services.assessment.interfaces.IAssessmentsService;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IDailyGoalsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import giuliacrepaldi.BStudent_Management.services.progression.PracticeProgressService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IPracticeProgressService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.ISkillCreditsService;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Svolgimento della Palestra e suoi punti (blueprint principale §7.2, §10.1; Blueprint Svolgimento §2, §6, §7)
class PracticePlayTest {

    private static final long STUDENT = 7L;
    private static final long PRACTICE = 30L;
    private static final long ENTRY = 40L;
    private static final long ATTEMPT = 50L;
    private static final long LATIN = 1L;
    private static final long GRAMMAR = 10L;

    private ExerciseAttemptsRepository attempts;
    private ExerciseAssessmentsRepository assessments;
    private PracticeExercisesRepository practiceExercises;
    private QuestionVersionsRepository versions;
    private IAssessmentsService assessmentsService;
    private IPracticeProgressService progress;
    private ISkillLevelsService skillLevels;
    private IEntitlementService entitlement;
    private PracticeAttemptsService service;

    private final QuestionBankEntry entry = new QuestionBankEntry();
    private PracticeExercise exercise;

    @BeforeEach
    void setUp() {
        attempts = mock(ExerciseAttemptsRepository.class);
        assessments = mock(ExerciseAssessmentsRepository.class);
        practiceExercises = mock(PracticeExercisesRepository.class);
        versions = mock(QuestionVersionsRepository.class);
        assessmentsService = mock(IAssessmentsService.class);
        progress = mock(IPracticeProgressService.class);
        skillLevels = mock(ISkillLevelsService.class);
        entitlement = mock(IEntitlementService.class);
        StudentsRepository students = mock(StudentsRepository.class);
        when(students.getReferenceById(STUDENT)).thenReturn(student());

        Subject latin = new Subject();
        latin.setSubjectId(LATIN);
        latin.setStatus(SubjectStatus.published);
        entry.setQuestionBankEntryId(ENTRY);
        entry.setStatus(ContentStatus.published);
        entry.setTitle("La prima declinazione");
        exercise = PracticeExercise.builder().practiceExerciseId(PRACTICE).questionBankEntry(entry)
                .status(ContentStatus.published).accessTier(AccessTier.free).maxPoints(new BigDecimal("10.00"))
                .skill(Skill.builder().skillId(GRAMMAR).skillName("Grammatica").build())
                .topic(Topic.builder().topicId(80L).subject(latin).build()).build();

        when(practiceExercises.findById(PRACTICE)).thenReturn(Optional.of(exercise));
        when(practiceExercises.findByQuestionBankEntry_QuestionBankEntryId(ENTRY)).thenReturn(Optional.of(exercise));
        when(attempts.findOpenPractice(eq(STUDENT), eq(ENTRY), any())).thenReturn(Optional.empty());
        when(skillLevels.getLock(STUDENT, PRACTICE)).thenReturn(new PracticeLockDTO(PRACTICE, 1, false, null, null));
        when(versions.findPublished(ENTRY)).thenReturn(Optional.of(QuestionVersion.builder().questionVersionId(60L).build()));
        when(assessmentsService.newAnswerOrder(60L)).thenReturn(List.of(3L, 1L, 2L));
        when(assessmentsService.toView(any())).thenReturn(new ExerciseViewDTO("La prima declinazione", null, "...", null,
                AssessmentMethod.automatic, List.of()));
        when(attempts.save(any(ExerciseAttempt.class))).thenAnswer(invocation -> {
            ExerciseAttempt saved = invocation.getArgument(0);
            saved.setExerciseAttemptId(ATTEMPT);
            return saved;
        });

        service = new PracticeAttemptsService(attempts, assessments, practiceExercises, versions, students,
                assessmentsService, progress, skillLevels, entitlement);
    }

    private static Student student() {
        Student student = new Student();
        student.setStudentId(STUDENT);
        return student;
    }

    private ExerciseAttempt attempt(ExerciseAttemptStatus status) {
        ExerciseAttempt attempt = ExerciseAttempt.builder().exerciseAttemptId(ATTEMPT).student(student())
                .context(ExerciseContext.practice).questionBankEntry(entry).status(status)
                .configurationSnapshot(AttemptJson.writeConfiguration("practiceExerciseId", PRACTICE,
                        new BigDecimal("10.00"), List.of()))
                .build();
        when(attempts.findById(ATTEMPT)).thenReturn(Optional.of(attempt));
        return attempt;
    }

    // ------------------------------------------------------------------ Avvio

    @Test
    void unTentativoNuovoConservaVersionePuntiEOrdineDelleOpzioni() {
        PracticeAttemptDTO dto = service.start(STUDENT, PRACTICE);

        assertThat(dto.status()).isEqualTo(ExerciseAttemptStatus.active);
        assertThat(dto.answer().isEmpty()).isTrue();
        verify(attempts).save(any(ExerciseAttempt.class));
    }

    @Test
    void unaVoceBloccataPerLivelloNonSiAvviaNemmenoConoscendoneLIndirizzo() {
        when(skillLevels.getLock(STUDENT, PRACTICE))
                .thenReturn(new PracticeLockDTO(PRACTICE, 4, true, 3, "Si sblocca al livello 3 di Grammatica"));
        assertThatThrownBy(() -> service.start(STUDENT, PRACTICE))
                .isInstanceOf(ConflictException.class).hasMessage("Si sblocca al livello 3 di Grammatica");
        verify(attempts, never()).save(any());
    }

    @Test
    void unaVocePremiumRichiedeLAbbonamento() {
        exercise.setAccessTier(AccessTier.premium);
        assertThatThrownBy(() -> service.start(STUDENT, PRACTICE)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void conUnaBozzaApertaSiRiprendeQuella() {
        ExerciseAttempt draft = attempt(ExerciseAttemptStatus.saved);
        draft.setResponse(AttemptJson.writeAnswer(new AnswerDTO(null, "rosam")));
        when(attempts.findOpenPractice(eq(STUDENT), eq(ENTRY), any())).thenReturn(Optional.of(draft));

        PracticeAttemptDTO dto = service.start(STUDENT, PRACTICE);

        assertThat(dto.exerciseAttemptId()).isEqualTo(ATTEMPT);
        assertThat(dto.answer().text()).isEqualTo("rosam");
        verify(attempts, never()).save(any());
    }

    @Test
    void unaConsegnaInCorrezioneNonSiRifa() {
        ExerciseAttempt inReview = attempt(ExerciseAttemptStatus.pending_review);
        when(attempts.findOpenPractice(eq(STUDENT), eq(ENTRY), any())).thenReturn(Optional.of(inReview));
        assertThatThrownBy(() -> service.start(STUDENT, PRACTICE))
                .isInstanceOf(ConflictException.class).hasMessageContaining("in correzione");
    }

    // ------------------------------------------------------------------ Consegna

    @Test
    void laCorrezioneAutomaticaDaSubitoLEsito() {
        ExerciseAttempt attempt = attempt(ExerciseAttemptStatus.active);
        when(assessmentsService.isAutomatic(attempt)).thenReturn(true);
        ExerciseOutcomeDTO outcome = new ExerciseOutcomeDTO(new BigDecimal("100"), new BigDecimal("60"), true, "Giusto",
                List.of("rosam"), null, null, null, null, new BigDecimal("10.00"), null);
        when(progress.onAssessed(STUDENT, ATTEMPT)).thenReturn(outcome);

        PracticeAttemptDTO dto = service.submit(STUDENT, ATTEMPT, new AnswerDTO(null, "rosam"));

        assertThat(dto.outcome()).isEqualTo(outcome);
        verify(assessmentsService).assessAutomatically(attempt);
        assertThat(attempt.getSubmittedAt()).isNotNull();
    }

    @Test
    void conLaCorrezioneDellaDocenteLaRispostaSiBloccaESiPuoAnnullare() {
        ExerciseAttempt attempt = attempt(ExerciseAttemptStatus.active);
        when(assessmentsService.isAutomatic(attempt)).thenReturn(false);

        PracticeAttemptDTO sent = service.submit(STUDENT, ATTEMPT, new AnswerDTO(null, "Rosa est pulchra"));
        assertThat(sent.status()).isEqualTo(ExerciseAttemptStatus.pending_review);
        assertThat(sent.outcome()).isNull();
        verify(assessmentsService, never()).assessAutomatically(any());

        // "Annulla invio": torna una bozza con la stessa risposta, modificabile (Svolgimento §7)
        PracticeAttemptDTO cancelled = service.cancelSubmission(STUDENT, ATTEMPT);
        assertThat(cancelled.status()).isEqualTo(ExerciseAttemptStatus.saved);
        assertThat(cancelled.answer().text()).isEqualTo("Rosa est pulchra");
        assertThat(attempt.getSubmittedAt()).isNull();
    }

    @Test
    void dopoLEsitoDellaDocenteLInvioNonSiAnnulla() {
        attempt(ExerciseAttemptStatus.assessed);
        assertThatThrownBy(() -> service.cancelSubmission(STUDENT, ATTEMPT))
                .isInstanceOf(ConflictException.class).hasMessageContaining("definitivo");
    }

    @Test
    void ilDoppioInvioNonRivalutaNeRiaccredita() {
        ExerciseAttempt attempt = attempt(ExerciseAttemptStatus.assessed);
        ExerciseAssessment done = ExerciseAssessment.builder().exerciseAttempt(attempt).build();
        when(assessments.findById(ATTEMPT)).thenReturn(Optional.of(done));

        service.submit(STUDENT, ATTEMPT, new AnswerDTO(null, "rosam"));

        verify(assessmentsService, never()).assessAutomatically(any());
        verify(progress, never()).onAssessed(anyLong(), anyLong());
        verify(assessmentsService).toOutcome(attempt, done);
    }

    @Test
    void unEsercizioRitiratoChiudeLaBozzaSenzaEsito() {
        ExerciseAttempt attempt = attempt(ExerciseAttemptStatus.saved);
        entry.setStatus(ContentStatus.withdrawn);

        assertThatThrownBy(() -> service.submit(STUDENT, ATTEMPT, new AnswerDTO(null, "rosam")))
                .isInstanceOf(ConflictException.class).hasMessageContaining("non è più disponibile");
        assertThat(attempt.getStatus()).isEqualTo(ExerciseAttemptStatus.abandoned);
        assertThat(attempt.getCompletedAt()).isNotNull();
    }

    @Test
    void uscireSenzaSalvareChiudeIlTentativo() {
        ExerciseAttempt attempt = attempt(ExerciseAttemptStatus.active);
        service.leave(STUDENT, ATTEMPT);
        assertThat(attempt.getStatus()).isEqualTo(ExerciseAttemptStatus.abandoned);
    }

    // ------------------------------------------------------------------ Punti e record

    private PracticeProgressService progressService(ISkillCreditsService credits, PracticeRecordsRepository records,
                                                    IStudentXpService xp) {
        StudentsRepository students = mock(StudentsRepository.class);
        IDailyGoalsService goals = mock(IDailyGoalsService.class);
        when(assessmentsService.toOutcome(any(), any())).thenReturn(new ExerciseOutcomeDTO(new BigDecimal("87"),
                new BigDecimal("60"), true, "ok", List.of(), null, null, null, null, BigDecimal.ZERO, null));
        return new PracticeProgressService(assessments, practiceExercises, records, students, assessmentsService, credits,
                goals, xp, mock(EntityManager.class));
    }

    // Registro dei punti che non trova miglioramenti
    private static ISkillCreditsService noImprovement() {
        ISkillCreditsService credits = mock(ISkillCreditsService.class);
        when(credits.creditImprovement(anyLong(), anyLong(), anyLong(), any(), anyLong(), any(), anyString()))
                .thenReturn(BigDecimal.ZERO);
        return credits;
    }

    private void assessed(String percentage) {
        ExerciseAttempt attempt = attempt(ExerciseAttemptStatus.assessed);
        when(assessments.findById(ATTEMPT)).thenReturn(Optional.of(ExerciseAssessment.builder()
                .exerciseAttemptId(ATTEMPT).exerciseAttempt(attempt).percentage(new BigDecimal(percentage))
                .passingThreshold(new BigDecimal("60.00")).validatedAt(OffsetDateTime.now()).build()));
    }

    @Test
    void unRisultatoSopraSogliaAccreditaIlContributoNellaMateriaDellaVoce() {
        assessed("87.00");
        ISkillCreditsService credits = mock(ISkillCreditsService.class);
        when(credits.creditImprovement(anyLong(), anyLong(), anyLong(), any(), anyLong(), any(), anyString()))
                .thenReturn(new BigDecimal("8.70"));
        IStudentXpService xp = mock(IStudentXpService.class);

        ExerciseOutcomeDTO outcome = progressService(credits, mock(PracticeRecordsRepository.class), xp)
                .onAssessed(STUDENT, ATTEMPT);

        verify(credits).creditImprovement(STUDENT, LATIN, GRAMMAR, SourceKind.practice, PRACTICE,
                new BigDecimal("8.70"), "practice:" + ATTEMPT);
        assertThat(outcome.pointsEarned()).isEqualByComparingTo("8.70");
        verify(xp).onSkillPointsCredited(STUDENT);
    }

    @Test
    void sottoSogliaNessunPuntoENessunRecord() {
        assessed("55.00");
        ISkillCreditsService credits = mock(ISkillCreditsService.class);
        PracticeRecordsRepository records = mock(PracticeRecordsRepository.class);

        ExerciseOutcomeDTO outcome = progressService(credits, records, mock(IStudentXpService.class))
                .onAssessed(STUDENT, ATTEMPT);

        assertThat(outcome.pointsEarned()).isEqualByComparingTo("0");
        verify(credits, never()).creditImprovement(anyLong(), anyLong(), anyLong(), any(), anyLong(), any(), anyString());
        verify(records, never()).findById(any());
    }

    @Test
    void unRisultatoPeggioreNonToccaIlRecord() {
        assessed("80.00");
        PracticeRecordsRepository records = mock(PracticeRecordsRepository.class);
        PracticeRecord record = PracticeRecord.builder().percentage(new BigDecimal("89.00"))
                .maxPointsSnapshot(new BigDecimal("10.00")).points(new BigDecimal("8.90")).build();
        when(records.findById(any())).thenReturn(Optional.of(record));

        progressService(noImprovement(), records, mock(IStudentXpService.class)).onAssessed(STUDENT, ATTEMPT);

        assertThat(record.getPoints()).isEqualByComparingTo("8.90");
        assertThat(record.getPercentage()).isEqualByComparingTo("89");
    }

    @Test
    void ilRisultatoMiglioreAggiornaIlRecord() {
        assessed("100.00");
        PracticeRecordsRepository records = mock(PracticeRecordsRepository.class);
        PracticeRecord record = PracticeRecord.builder().percentage(new BigDecimal("89.00"))
                .maxPointsSnapshot(new BigDecimal("10.00")).points(new BigDecimal("8.90")).build();
        when(records.findById(any())).thenReturn(Optional.of(record));

        progressService(noImprovement(), records, mock(IStudentXpService.class)).onAssessed(STUDENT, ATTEMPT);

        assertThat(record.getPoints()).isEqualByComparingTo("10");
    }

    @Test
    void laRispostaSalvataVieneControllataMaPuoEssereVuota() {
        attempt(ExerciseAttemptStatus.active);
        service.saveDraft(STUDENT, ATTEMPT, AnswerDTO.empty());
        verify(assessmentsService).validateAnswer(any(), any(), eq(false));
        verify(assessmentsService, never()).validateAnswer(any(), any(), eq(true));
        // Salvare non è consegnare: nessun esito e nessun punto
        verify(progress, never()).onAssessed(anyLong(), anyLong());
    }
}
