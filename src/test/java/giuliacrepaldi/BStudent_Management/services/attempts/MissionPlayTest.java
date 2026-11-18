package giuliacrepaldi.BStudent_Management.services.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionCheckpoint;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRun;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRunLesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.FailureReason;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionRunStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionPlayDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionStepOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintChargesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.CourseResultDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.MissionPassedDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.SkillPointsDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionCheckpointsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionRunLessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionRunsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonPagesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionStepsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionVersionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.services.assessment.interfaces.IAssessmentsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IHintsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.services.notebook.interfaces.INotebookService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IMissionProgressService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentMissionsService;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Svolgimento delle missioni (blueprint principale §9; Blueprint Svolgimento §1, §2, §6, §9).
// Un piccolo database in memoria: missione "Le declinazioni" di Latino con
// lezione 1 -> esercizio 10 -> lezione 2 -> esercizio 20
class MissionPlayTest {

    private static final long STUDENT = 7L;
    private static final long LESSON_1 = 1L;
    private static final long LESSON_2 = 2L;
    private static final long EXERCISE_10 = 10L;
    private static final long EXERCISE_20 = 20L;
    private static final long TEN_MINUTES = 600_000L;
    private static final BigDecimal SIXTY = new BigDecimal("60.00");

    // Il "database"
    private final List<MissionRun> runs = new ArrayList<>();
    private final List<MissionAttempt> missionAttempts = new ArrayList<>();
    private final List<ExerciseAttempt> exerciseAttempts = new ArrayList<>();
    private final List<ExerciseAssessment> assessments = new ArrayList<>();
    private final List<MissionRunLesson> readLessons = new ArrayList<>();
    private final List<MissionCheckpoint> checkpoints = new ArrayList<>();
    private final List<MissionStep> steps = new ArrayList<>();
    private int lives = 5;
    // Percentuale che darà la prossima correzione automatica
    private BigDecimal nextPercentage = PlayRules.FULL;
    // Esercizi corretti dalla docente
    private final Set<Long> teacherExercises = new HashSet<>();

    private ILivesService livesService;
    private IMissionProgressService progress;
    private INotebookService notebook;
    private IStudentMissionsService studentMissions;
    private MissionRunsService service;

    private Subject latin;
    private Mission mission;
    private MissionExercise exercise10;
    private MissionExercise exercise20;

    @BeforeEach
    void setUp() {
        latin = new Subject();
        latin.setSubjectId(100L);
        latin.setSubjectName("Latino");
        latin.setStatus(SubjectStatus.published);
        Course course = new Course();
        course.setCourseId(200L);
        course.setSlug("latino-1");
        course.setSubject(latin);
        mission = mission(300L, "le-declinazioni", "Le declinazioni", course);
        Lesson lesson1 = lesson(LESSON_1, "La prima declinazione");
        Lesson lesson2 = lesson(LESSON_2, "La seconda declinazione");
        exercise10 = exercise(EXERCISE_10, 40L);
        exercise20 = exercise(EXERCISE_20, 41L);
        steps.addAll(List.of(MissionStep.of(lesson1, 1), MissionStep.of(exercise10, 2), MissionStep.of(lesson2, 3),
                MissionStep.of(exercise20, 4)));

        MissionRunsRepository runsRepository = mock(MissionRunsRepository.class);
        when(runsRepository.save(any(MissionRun.class))).thenAnswer(invocation -> {
            MissionRun run = invocation.getArgument(0);
            run.setMissionRunId((long) runs.size() + 1);
            runs.add(run);
            return run;
        });
        when(runsRepository.findByMission(eq(STUDENT), anyLong(), any())).thenAnswer(invocation -> {
            long missionId = invocation.getArgument(1);
            Collection<MissionRunStatus> statuses = invocation.getArgument(2);
            return runs.stream().filter(r -> r.getMission().getMissionId() == missionId && statuses.contains(r.getStatus()))
                    .sorted(Comparator.comparing(MissionRun::getMissionRunId).reversed()).toList();
        });
        when(runsRepository.findFirstByStudent_StudentIdAndSubject_SubjectIdAndStatus(eq(STUDENT), anyLong(), any()))
                .thenAnswer(invocation -> runs.stream()
                        .filter(r -> r.getSubject().getSubjectId().equals(invocation.getArgument(1))
                                && r.getStatus() == invocation.getArgument(2))
                        .findFirst());
        when(runsRepository.findByStudent_StudentIdAndStatusInOrderByMissionRunId(eq(STUDENT), any()))
                .thenAnswer(invocation -> runs.stream()
                        .filter(r -> ((Collection<?>) invocation.getArgument(1)).contains(r.getStatus())).toList());

        MissionAttemptsRepository attemptsRepository = mock(MissionAttemptsRepository.class);
        when(attemptsRepository.save(any(MissionAttempt.class))).thenAnswer(invocation -> {
            MissionAttempt attempt = invocation.getArgument(0);
            attempt.setMissionAttemptId((long) missionAttempts.size() + 1);
            missionAttempts.add(attempt);
            return attempt;
        });
        when(attemptsRepository.findById(anyLong())).thenAnswer(invocation -> missionAttempts.stream()
                .filter(a -> a.getMissionAttemptId().equals(invocation.getArgument(0))).findFirst());
        when(attemptsRepository.findFirstByMissionRun_MissionRunIdOrderByAttemptNumberDesc(anyLong()))
                .thenAnswer(invocation -> missionAttempts.stream()
                        .filter(a -> a.getMissionRun().getMissionRunId().equals(invocation.getArgument(0)))
                        .max(Comparator.comparingInt(MissionAttempt::getAttemptNumber)));

        ExerciseAttemptsRepository exerciseRepository = mock(ExerciseAttemptsRepository.class);
        when(exerciseRepository.save(any(ExerciseAttempt.class))).thenAnswer(invocation -> {
            ExerciseAttempt attempt = invocation.getArgument(0);
            attempt.setExerciseAttemptId((long) exerciseAttempts.size() + 1000);
            exerciseAttempts.add(attempt);
            return attempt;
        });
        when(exerciseRepository.findById(anyLong())).thenAnswer(invocation -> exerciseAttempts.stream()
                .filter(a -> a.getExerciseAttemptId().equals(invocation.getArgument(0))).findFirst());
        when(exerciseRepository.findFirstByMissionAttempt_MissionAttemptIdAndStatusIn(anyLong(), any()))
                .thenAnswer(invocation -> exerciseAttempts.stream()
                        .filter(a -> a.getMissionAttempt().getMissionAttemptId().equals(invocation.getArgument(0))
                                && ((Collection<?>) invocation.getArgument(1)).contains(a.getStatus()))
                        .findFirst());

        ExerciseAssessmentsRepository assessmentsRepository = mock(ExerciseAssessmentsRepository.class);
        when(assessmentsRepository.findById(anyLong())).thenAnswer(invocation -> assessments.stream()
                .filter(a -> a.getExerciseAttempt().getExerciseAttemptId().equals(invocation.getArgument(0))).findFirst());
        when(assessmentsRepository.existsById(anyLong())).thenAnswer(invocation -> assessments.stream()
                .anyMatch(a -> a.getExerciseAttempt().getExerciseAttemptId().equals(invocation.getArgument(0))));

        MissionCheckpointsRepository checkpointsRepository = mock(MissionCheckpointsRepository.class);
        when(checkpointsRepository.findExerciseIds(anyLong())).thenAnswer(invocation -> checkpoints.stream()
                .filter(c -> c.getId().getMissionRunId().equals(invocation.getArgument(0)))
                .map(c -> c.getId().getMissionExerciseId()).toList());
        MissionRunLessonsRepository runLessonsRepository = mock(MissionRunLessonsRepository.class);
        when(runLessonsRepository.findLessonIds(anyLong())).thenAnswer(invocation -> readLessons.stream()
                .filter(l -> l.getId().getMissionRunId().equals(invocation.getArgument(0)))
                .map(l -> l.getId().getLessonId()).toList());
        MissionStepsRepository stepsRepository = mock(MissionStepsRepository.class);
        when(stepsRepository.findByMissionOrdered(anyLong())).thenAnswer(invocation -> List.copyOf(steps));

        EntityManager entityManager = mock(EntityManager.class);
        doAnswer(invocation -> {
            Object entity = invocation.getArgument(0);
            if (entity instanceof MissionRunLesson lesson) {
                readLessons.add(lesson);
            } else if (entity instanceof MissionCheckpoint checkpoint) {
                checkpoints.add(checkpoint);
            }
            return null;
        }).when(entityManager).persist(any());

        QuestionVersionsRepository versions = mock(QuestionVersionsRepository.class);
        when(versions.findPublished(anyLong())).thenAnswer(invocation ->
                Optional.of(QuestionVersion.builder().questionVersionId(invocation.getArgument(0)).build()));
        StudentsRepository students = mock(StudentsRepository.class);
        when(students.getReferenceById(STUDENT)).thenReturn(student());

        studentMissions = mock(IStudentMissionsService.class);
        when(studentMissions.findPlayableMission(STUDENT, "latino-1", "le-declinazioni")).thenReturn(mission);

        livesService = mock(ILivesService.class);
        when(livesService.canStartMission(STUDENT)).thenAnswer(invocation -> lives > 0);
        when(livesService.getLives(STUDENT)).thenAnswer(invocation -> new LivesDTO(lives, 5, null));
        when(livesService.onMissionAttemptFailed(eq(STUDENT), anyLong())).thenAnswer(invocation -> {
            lives = Math.max(0, lives - 1);
            return new LivesDTO(lives, 5, null);
        });
        IHintsService hints = mock(IHintsService.class);
        when(hints.getCharges(STUDENT)).thenReturn(HintChargesDTO.none());

        progress = mock(IMissionProgressService.class);
        when(progress.onExerciseAssessed(eq(STUDENT), anyLong())).thenReturn(new SkillPointsDTO(new BigDecimal("0.70"), List.of()));
        when(progress.onMissionPassed(eq(STUDENT), anyLong())).thenReturn(new MissionPassedDTO(PlayRules.FULL, true,
                new BigDecimal("20"), List.of(), new CourseResultDTO(200L, "Latino 1", 1, 3, PlayRules.FULL, false, List.of())));
        notebook = mock(INotebookService.class);

        service = new MissionRunsService(runsRepository, attemptsRepository, exerciseRepository, assessmentsRepository,
                checkpointsRepository, runLessonsRepository, stepsRepository, mock(LessonPagesRepository.class), versions,
                students, studentMissions, fakeAssessments(), progress, livesService, hints, notebook, entityManager);
    }

    // Correzione automatica finta: dà nextPercentage; i due esercizi della docente non si correggono da soli
    private IAssessmentsService fakeAssessments() {
        IAssessmentsService fake = mock(IAssessmentsService.class);
        when(fake.newAnswerOrder(anyLong())).thenReturn(List.of());
        when(fake.isAutomatic(any())).thenAnswer(invocation -> !teacherExercises.contains(
                ((ExerciseAttempt) invocation.getArgument(0)).getMissionExercise().getMissionExerciseId()));
        when(fake.assessAutomatically(any())).thenAnswer(invocation -> {
            ExerciseAttempt attempt = invocation.getArgument(0);
            attempt.setStatus(ExerciseAttemptStatus.assessed);
            attempt.setCompletedAt(OffsetDateTime.now());
            attempt.setTimerStartedAt(null);
            ExerciseAssessment assessment = ExerciseAssessment.builder().exerciseAttempt(attempt)
                    .assessmentMethod(AssessmentMethod.automatic).percentage(nextPercentage).passingThreshold(SIXTY)
                    .passed(PlayRules.isPassed(nextPercentage, SIXTY)).feedback("…").build();
            assessments.add(assessment);
            return assessment;
        });
        when(fake.toOutcome(any(), any())).thenAnswer(invocation -> {
            ExerciseAssessment assessment = invocation.getArgument(1);
            return new ExerciseOutcomeDTO(assessment.getPercentage(), SIXTY,
                    PlayRules.isPassed(assessment.getPercentage(), SIXTY), "…", List.of(), null, null, null, null,
                    PlayRules.ZERO, null);
        });
        return fake;
    }

    // ------------------------------------------------------------------ Dati

    private static Student student() {
        Student student = new Student();
        student.setStudentId(STUDENT);
        return student;
    }

    private static Mission mission(long id, String slug, String title, Course course) {
        Mission mission = new Mission();
        mission.setMissionId(id);
        mission.setSlug(slug);
        mission.setMissionTitle(title);
        mission.setCourse(course);
        return mission;
    }

    private Lesson lesson(long id, String title) {
        Lesson lesson = new Lesson();
        lesson.setLessonId(id);
        lesson.setLessonTitle(title);
        lesson.setStatus(ContentStatus.published);
        lesson.setMission(mission);
        return lesson;
    }

    private MissionExercise exercise(long id, long entryId) {
        QuestionBankEntry entry = new QuestionBankEntry();
        entry.setQuestionBankEntryId(entryId);
        entry.setStatus(ContentStatus.published);
        entry.setTitle("Esercizio " + id);
        return MissionExercise.builder().missionExerciseId(id).mission(mission).questionBankEntry(entry)
                .status(ContentStatus.published).allowedDurationMs(TEN_MINUTES).maxPoints(new BigDecimal("10.00")).build();
    }

    private MissionPlayDTO start() {
        return service.start(STUDENT, "latino-1", "le-declinazioni");
    }

    private static AnswerDTO answer(String text) {
        return new AnswerDTO(null, text);
    }

    // Avvia e legge la prima lezione: compare l'esercizio 10
    private MissionPlayDTO startAndReachFirstExercise() {
        MissionPlayDTO state = start();
        return service.completeLesson(STUDENT, state.missionAttemptId(), LESSON_1).state();
    }

    private ExerciseAttempt openExercise() {
        return exerciseAttempts.stream()
                .filter(a -> a.getStatus() == ExerciseAttemptStatus.active || a.getStatus() == ExerciseAttemptStatus.saved)
                .reduce((first, second) -> second).orElseThrow();
    }

    // ------------------------------------------------------------------ Avvio e lezioni

    @Test
    void laMissioneIniziaDallaPrimaLezioneSenzaConsumareVite() {
        MissionPlayDTO state = start();

        assertThat(state.status()).isEqualTo(MissionAttemptStatus.active);
        assertThat(state.currentStep()).isEqualTo(1);
        assertThat(state.exercise()).isNull();
        assertThat(runs.getFirst().getSubject()).isSameAs(latin);
        assertThat(lives).isEqualTo(5);
    }

    @Test
    void finitaLaLezioneCompareLEsercizioEIlTempoParte() {
        MissionPlayDTO state = startAndReachFirstExercise();

        assertThat(state.currentStep()).isEqualTo(2);
        assertThat(state.exercise().missionExerciseId()).isEqualTo(EXERCISE_10);
        assertThat(state.exercise().timerRunning()).isTrue();
        assertThat(state.exercise().remainingMs()).isBetween(TEN_MINUTES - 1_000, TEN_MINUTES);
        verify(notebook).acquireLesson(STUDENT, LESSON_1);
    }

    @Test
    void siLeggeSoloLaLezioneAttuale() {
        MissionPlayDTO state = start();

        assertThatThrownBy(() -> service.completeLesson(STUDENT, state.missionAttemptId(), LESSON_2))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void senzaViteNonSiParte() {
        lives = 0;

        assertThatThrownBy(this::start).isInstanceOf(ConflictException.class).hasMessageContaining("Palestra");
        assertThat(runs).isEmpty();
    }

    @Test
    void unaMissioneInCorsoPerMateria() {
        start();
        Mission other = mission(301L, "i-verbi", "I verbi", mission.getCourse());
        when(studentMissions.findPlayableMission(STUDENT, "latino-1", "i-verbi")).thenReturn(other);

        assertThatThrownBy(() -> service.start(STUDENT, "latino-1", "i-verbi"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Latino").hasMessageContaining("Le declinazioni");
    }

    @Test
    void riaprireLaStessaMissioneLaRiprende() {
        MissionPlayDTO first = startAndReachFirstExercise();

        MissionPlayDTO again = start();

        assertThat(again.missionAttemptId()).isEqualTo(first.missionAttemptId());
        assertThat(runs).hasSize(1);
        assertThat(again.exercise().exerciseAttemptId()).isEqualTo(first.exercise().exerciseAttemptId());
    }

    // ------------------------------------------------------------------ Consegna

    @Test
    void esercizioSuperatoDiventaCheckpointESiVaAllaLezioneDopo() {
        MissionPlayDTO state = startAndReachFirstExercise();

        MissionStepOutcomeDTO outcome = service.submitExercise(STUDENT, state.exercise().exerciseAttemptId(), answer("rosam"));

        assertThat(outcome.result()).isEqualTo("passed");
        assertThat(outcome.outcome().pointsEarned()).isEqualByComparingTo("0.70");
        assertThat(checkpoints).hasSize(1);
        assertThat(outcome.state().currentStep()).isEqualTo(3);
        assertThat(outcome.state().lesson()).isNotNull();
        assertThat(outcome.lifeLost()).isFalse();
    }

    @Test
    void tuttiIPassiFattiLaMissioneESuperata() {
        MissionPlayDTO state = startAndReachFirstExercise();
        service.submitExercise(STUDENT, state.exercise().exerciseAttemptId(), answer("rosam"));
        MissionPlayDTO second = service.completeLesson(STUDENT, state.missionAttemptId(), LESSON_2).state();

        MissionStepOutcomeDTO outcome = service.submitExercise(STUDENT, second.exercise().exerciseAttemptId(), answer("rosas"));

        assertThat(outcome.result()).isEqualTo("mission_passed");
        assertThat(outcome.missionPassed().xpEarned()).isEqualByComparingTo("20");
        assertThat(outcome.state().status()).isEqualTo(MissionAttemptStatus.passed);
        assertThat(runs.getFirst().getStatus()).isEqualTo(MissionRunStatus.passed);
        verify(progress).onMissionPassed(STUDENT, state.missionAttemptId());
    }

    @Test
    void sottoLaSogliaSiPerdeUnaVitaESiRipartedallEsercizioConTempoPieno() {
        MissionPlayDTO state = startAndReachFirstExercise();
        nextPercentage = PlayRules.ZERO;

        MissionStepOutcomeDTO outcome = service.submitExercise(STUDENT, state.exercise().exerciseAttemptId(), answer("rosa"));

        assertThat(outcome.result()).isEqualTo("failed");
        assertThat(outcome.lifeLost()).isTrue();
        assertThat(lives).isEqualTo(4);
        assertThat(outcome.state().status()).isEqualTo(MissionAttemptStatus.failed);
        assertThat(outcome.state().failureReason()).isEqualTo(FailureReason.below_threshold);
        // I punti skill maturano anche sotto la soglia (§5)
        verify(progress).onExerciseAssessed(STUDENT, state.exercise().exerciseAttemptId());

        MissionPlayDTO retry = start();

        assertThat(retry.attemptNumber()).isEqualTo(2);
        assertThat(retry.currentStep()).isEqualTo(2);
        assertThat(retry.exercise().answer().isEmpty()).isTrue();
        assertThat(retry.exercise().remainingMs()).isGreaterThan(TEN_MINUTES - 1_000);
        assertThat(runs).hasSize(1);
    }

    @Test
    void ilDoppioInvioNonHaDoppiEffetti() {
        MissionPlayDTO state = startAndReachFirstExercise();
        nextPercentage = PlayRules.ZERO;
        long exerciseId = state.exercise().exerciseAttemptId();
        service.submitExercise(STUDENT, exerciseId, answer("rosa"));

        MissionStepOutcomeDTO again = service.submitExercise(STUDENT, exerciseId, answer("rosa"));

        assertThat(again.result()).isEqualTo("failed");
        assertThat(again.lifeLost()).isFalse();
        assertThat(lives).isEqualTo(4);
        verify(progress, times(1)).onExerciseAssessed(STUDENT, exerciseId);
    }

    // ------------------------------------------------------------------ Tempo

    @Test
    void aTempoFinitoIlTentativoFallisceConUnaSolaVita() {
        MissionPlayDTO state = startAndReachFirstExercise();
        ExerciseAttempt exercise = openExercise();
        // Ultima sincronizzazione 20 secondi fa con 10 secondi rimasti
        exercise.setRemainingTimeMs(10_000L);
        exercise.setTimerStartedAt(OffsetDateTime.now().minusSeconds(20));

        MissionPlayDTO after = service.getState(STUDENT, state.missionAttemptId());
        service.getState(STUDENT, state.missionAttemptId());

        assertThat(after.status()).isEqualTo(MissionAttemptStatus.failed);
        assertThat(after.failureReason()).isEqualTo(FailureReason.time_expired);
        assertThat(exercise.getStatus()).isEqualTo(ExerciseAttemptStatus.expired);
        assertThat(lives).isEqualTo(4);
    }

    @Test
    void unaConsegnaInRitardoDiUnAttimoVieneAccettata() {
        MissionPlayDTO state = startAndReachFirstExercise();
        ExerciseAttempt exercise = openExercise();
        exercise.setRemainingTimeMs(5_000L);
        exercise.setTimerStartedAt(OffsetDateTime.now().minusSeconds(6));

        MissionStepOutcomeDTO outcome = service.submitExercise(STUDENT, state.exercise().exerciseAttemptId(), answer("rosam"));

        assertThat(outcome.result()).isEqualTo("passed");
    }

    @Test
    void chiudereIlBrowserMetteLaMissioneInPausaSenzaPerdereVite() {
        MissionPlayDTO state = startAndReachFirstExercise();
        ExerciseAttempt exercise = openExercise();
        // Ultima sincronizzazione due ore fa con 6 minuti rimasti
        exercise.setRemainingTimeMs(360_000L);
        exercise.setTimerStartedAt(OffsetDateTime.now().minusHours(2));

        MissionPlayDTO after = service.getState(STUDENT, state.missionAttemptId());

        assertThat(after.status()).isEqualTo(MissionAttemptStatus.saved);
        assertThat(after.exercise().timerRunning()).isFalse();
        assertThat(after.exercise().remainingMs()).isEqualTo(360_000L - MissionRunsService.TOLERANCE_MS);
        assertThat(lives).isEqualTo(5);
    }

    @Test
    void salvareERiprendereConservaRispostaETempo() {
        MissionPlayDTO state = startAndReachFirstExercise();
        openExercise().setRemainingTimeMs(240_000L);

        MissionPlayDTO paused = service.pause(STUDENT, state.missionAttemptId(), answer("ros"));

        assertThat(paused.status()).isEqualTo(MissionAttemptStatus.saved);
        assertThat(paused.exercise().timerRunning()).isFalse();
        long remaining = paused.exercise().remainingMs();
        assertThat(remaining).isBetween(239_000L, 240_000L);

        MissionPlayDTO resumed = service.resume(STUDENT, state.missionAttemptId());

        assertThat(resumed.status()).isEqualTo(MissionAttemptStatus.active);
        assertThat(resumed.exercise().answer().text()).isEqualTo("ros");
        assertThat(resumed.exercise().timerRunning()).isTrue();
        assertThat(resumed.exercise().remainingMs()).isBetween(remaining - 1_000, remaining);
    }

    @Test
    void perRiprendereServeUnaVita() {
        MissionPlayDTO state = startAndReachFirstExercise();
        service.pause(STUDENT, state.missionAttemptId(), null);
        lives = 0;

        assertThatThrownBy(() -> service.resume(STUDENT, state.missionAttemptId())).isInstanceOf(ConflictException.class);
    }

    @Test
    void laSincronizzazioneConservaLaRispostaEIlTempoContinua() {
        MissionPlayDTO state = startAndReachFirstExercise();

        var sync = service.sync(STUDENT, state.exercise().exerciseAttemptId(), answer("ros"));

        assertThat(sync.status()).isEqualTo(MissionAttemptStatus.active);
        assertThat(AttemptJson.readAnswer(openExercise().getResponse()).text()).isEqualTo("ros");
        assertThat(openExercise().getTimerStartedAt()).isNotNull();
    }

    // ------------------------------------------------------------------ Abbandono

    @Test
    void abbandonareCostaUnaVitaESiRipartedaZero() {
        MissionPlayDTO state = startAndReachFirstExercise();

        service.abandon(STUDENT, state.missionAttemptId());

        assertThat(lives).isEqualTo(4);
        assertThat(runs.getFirst().getStatus()).isEqualTo(MissionRunStatus.abandoned);
        assertThat(openExerciseCount()).isZero();

        MissionPlayDTO fresh = start();

        assertThat(runs).hasSize(2);
        assertThat(fresh.currentStep()).isEqualTo(1);
    }

    private long openExerciseCount() {
        return exerciseAttempts.stream().filter(a -> a.getStatus() == ExerciseAttemptStatus.active
                || a.getStatus() == ExerciseAttemptStatus.saved).count();
    }

    // ------------------------------------------------------------------ Esercizio ritirato (§6)

    @Test
    void unEsercizioRitiratoMentreLoSiSvolgeVieneSaltatoSenzaPerdereVite() {
        MissionPlayDTO state = startAndReachFirstExercise();
        exercise10.getQuestionBankEntry().setStatus(ContentStatus.withdrawn);

        MissionPlayDTO after = service.getState(STUDENT, state.missionAttemptId());

        assertThat(exerciseAttempts.getFirst().getStatus()).isEqualTo(ExerciseAttemptStatus.abandoned);
        assertThat(after.currentStep()).isEqualTo(2);
        assertThat(after.lesson().lessonId()).isEqualTo(LESSON_2);
        assertThat(lives).isEqualTo(5);
    }

    @Test
    void seLUltimoEsercizioVieneRitiratoLaMissioneESuperataConQuelliRimasti() {
        MissionPlayDTO state = startAndReachFirstExercise();
        service.submitExercise(STUDENT, state.exercise().exerciseAttemptId(), answer("rosam"));
        MissionPlayDTO second = service.completeLesson(STUDENT, state.missionAttemptId(), LESSON_2).state();
        exercise20.getQuestionBankEntry().setStatus(ContentStatus.withdrawn);

        MissionStepOutcomeDTO outcome = service.submitExercise(STUDENT, second.exercise().exerciseAttemptId(), answer("rosas"));

        assertThat(outcome.result()).isEqualTo("mission_passed");
        assertThat(lives).isEqualTo(5);
        verify(progress, never()).onExerciseAssessed(STUDENT, second.exercise().exerciseAttemptId());
    }

    // ------------------------------------------------------------------ Esercizio della docente (§1, §2)

    @Test
    void consegnaAllaDocenteAnnullaInvioERitornoInPausaConIlTempoDellInvio() {
        teacherExercises.add(EXERCISE_20);
        MissionPlayDTO state = startAndReachFirstExercise();
        service.submitExercise(STUDENT, state.exercise().exerciseAttemptId(), answer("rosam"));
        MissionPlayDTO second = service.completeLesson(STUDENT, state.missionAttemptId(), LESSON_2).state();
        openExercise().setRemainingTimeMs(360_000L);

        MissionStepOutcomeDTO sent = service.submitExercise(STUDENT, second.exercise().exerciseAttemptId(), answer("Rosa est pulchra"));

        assertThat(sent.result()).isEqualTo("pending_review");
        assertThat(runs.getFirst().getStatus()).isEqualTo(MissionRunStatus.pending_review);
        assertThatThrownBy(() -> service.abandon(STUDENT, state.missionAttemptId())).isInstanceOf(ConflictException.class);

        MissionPlayDTO cancelled = service.cancelSubmission(STUDENT, second.exercise().exerciseAttemptId());

        assertThat(cancelled.status()).isEqualTo(MissionAttemptStatus.saved);
        assertThat(cancelled.exercise().answer().text()).isEqualTo("Rosa est pulchra");
        assertThat(cancelled.exercise().remainingMs()).isBetween(359_000L, 360_000L);
        assertThat(runs.getFirst().getStatus()).isEqualTo(MissionRunStatus.in_progress);
        assertThat(lives).isEqualTo(5);
    }

    @Test
    void inAttesaDellaDocenteSiPuoAprireUnAltraMissioneDellaStessaMateria() {
        teacherExercises.add(EXERCISE_20);
        MissionPlayDTO state = startAndReachFirstExercise();
        service.submitExercise(STUDENT, state.exercise().exerciseAttemptId(), answer("rosam"));
        MissionPlayDTO second = service.completeLesson(STUDENT, state.missionAttemptId(), LESSON_2).state();
        service.submitExercise(STUDENT, second.exercise().exerciseAttemptId(), answer("Rosa est pulchra"));
        Mission other = mission(301L, "i-verbi", "I verbi", mission.getCourse());
        when(studentMissions.findPlayableMission(STUDENT, "latino-1", "i-verbi")).thenReturn(other);

        service.start(STUDENT, "latino-1", "i-verbi");

        assertThat(runs).hasSize(2);
        // E allora "Annulla invio" non può riportare in corso la prima: una per materia
        assertThatThrownBy(() -> service.cancelSubmission(STUDENT, second.exercise().exerciseAttemptId()))
                .isInstanceOf(ConflictException.class).hasMessageContaining("I verbi");
    }

    @Test
    void lEsitoPositivoDellaDocenteChiudeLaMissione() {
        teacherExercises.add(EXERCISE_20);
        MissionPlayDTO state = startAndReachFirstExercise();
        service.submitExercise(STUDENT, state.exercise().exerciseAttemptId(), answer("rosam"));
        MissionPlayDTO second = service.completeLesson(STUDENT, state.missionAttemptId(), LESSON_2).state();
        long sentId = second.exercise().exerciseAttemptId();
        service.submitExercise(STUDENT, sentId, answer("Rosa est pulchra"));
        ExerciseAttempt sent = exerciseAttempts.stream().filter(a -> a.getExerciseAttemptId() == sentId).findFirst().orElseThrow();
        assessments.add(ExerciseAssessment.builder().exerciseAttempt(sent).assessmentMethod(AssessmentMethod.teacher)
                .percentage(new BigDecimal("75.00")).passingThreshold(SIXTY).feedback("Bene").build());

        MissionStepOutcomeDTO outcome = service.onTeacherAssessed(sentId);
        service.onTeacherAssessed(sentId);

        assertThat(outcome.result()).isEqualTo("mission_passed");
        assertThat(runs.getFirst().getStatus()).isEqualTo(MissionRunStatus.passed);
        verify(progress, times(1)).onMissionPassed(STUDENT, state.missionAttemptId());
    }

    @Test
    void lEsitoNegativoDellaDocenteCostaUnaVitaESiRifaSoloQuellEsercizio() {
        teacherExercises.add(EXERCISE_20);
        MissionPlayDTO state = startAndReachFirstExercise();
        service.submitExercise(STUDENT, state.exercise().exerciseAttemptId(), answer("rosam"));
        MissionPlayDTO second = service.completeLesson(STUDENT, state.missionAttemptId(), LESSON_2).state();
        long sentId = second.exercise().exerciseAttemptId();
        service.submitExercise(STUDENT, sentId, answer("Rosa est"));
        ExerciseAttempt sent = exerciseAttempts.stream().filter(a -> a.getExerciseAttemptId() == sentId).findFirst().orElseThrow();
        assessments.add(ExerciseAssessment.builder().exerciseAttempt(sent).assessmentMethod(AssessmentMethod.teacher)
                .percentage(new BigDecimal("40.00")).passingThreshold(SIXTY).feedback("Rivedi i casi").build());

        MissionStepOutcomeDTO outcome = service.onTeacherAssessed(sentId);

        assertThat(outcome.result()).isEqualTo("failed");
        assertThat(lives).isEqualTo(4);
        assertThat(runs.getFirst().getStatus()).isEqualTo(MissionRunStatus.in_progress);

        MissionPlayDTO retry = start();

        assertThat(retry.currentStep()).isEqualTo(4);
        assertThat(retry.exercise().missionExerciseId()).isEqualTo(EXERCISE_20);
    }
}
