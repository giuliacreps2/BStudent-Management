package giuliacrepaldi.BStudent_Management.services.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionCheckpoint;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRun;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRunLesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.attempts.FailureReason;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionRunStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.CurrentMissionDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.LessonPlayDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionExercisePlayDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionPlayDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionStepOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.MissionSyncDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonPageResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
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
import giuliacrepaldi.BStudent_Management.services.attempts.interfaces.IMissionRunsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IHintsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.services.notebook.interfaces.INotebookService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IMissionProgressService;
import giuliacrepaldi.BStudent_Management.services.student.StudentVisibility;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentMissionsService;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import giuliacrepaldi.BStudent_Management.utilities.MissionPath;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules.TimerState;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MissionRunsService implements IMissionRunsService {

    // Oltre questo tempo senza sincronizzazioni la missione va in pausa da sola (blueprint §9.2, nota tecnica)
    static final long TOLERANCE_MS = 45_000;
    // Margine per una consegna partita negli ultimi istanti e arrivata un attimo dopo per la rete
    static final long SUBMIT_GRACE_MS = 3_000;

    private static final List<ExerciseAttemptStatus> OPEN_EXERCISE =
            List.of(ExerciseAttemptStatus.active, ExerciseAttemptStatus.saved, ExerciseAttemptStatus.pending_review);
    private static final List<MissionRunStatus> OPEN_RUN = List.of(MissionRunStatus.in_progress, MissionRunStatus.pending_review);
    private static final DateTimeFormatter RECHARGE_FORMAT = DateTimeFormatter.ofPattern("dd/MM 'alle' HH:mm");
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    private final MissionRunsRepository runsRepository;
    private final MissionAttemptsRepository missionAttemptsRepository;
    private final ExerciseAttemptsRepository exerciseAttemptsRepository;
    private final ExerciseAssessmentsRepository assessmentsRepository;
    private final MissionCheckpointsRepository checkpointsRepository;
    private final MissionRunLessonsRepository runLessonsRepository;
    private final MissionStepsRepository stepsRepository;
    private final LessonPagesRepository lessonPagesRepository;
    private final QuestionVersionsRepository questionVersionsRepository;
    private final StudentsRepository studentsRepository;
    private final IStudentMissionsService studentMissionsService;
    private final IAssessmentsService assessmentsService;
    private final IMissionProgressService missionProgressService;
    private final ILivesService livesService;
    private final IHintsService hintsService;
    private final INotebookService notebookService;
    private final EntityManager entityManager;

    public MissionRunsService(MissionRunsRepository runsRepository, MissionAttemptsRepository missionAttemptsRepository,
                              ExerciseAttemptsRepository exerciseAttemptsRepository,
                              ExerciseAssessmentsRepository assessmentsRepository,
                              MissionCheckpointsRepository checkpointsRepository,
                              MissionRunLessonsRepository runLessonsRepository, MissionStepsRepository stepsRepository,
                              LessonPagesRepository lessonPagesRepository,
                              QuestionVersionsRepository questionVersionsRepository, StudentsRepository studentsRepository,
                              IStudentMissionsService studentMissionsService, IAssessmentsService assessmentsService,
                              IMissionProgressService missionProgressService, ILivesService livesService,
                              IHintsService hintsService, INotebookService notebookService, EntityManager entityManager) {
        this.runsRepository = runsRepository;
        this.missionAttemptsRepository = missionAttemptsRepository;
        this.exerciseAttemptsRepository = exerciseAttemptsRepository;
        this.assessmentsRepository = assessmentsRepository;
        this.checkpointsRepository = checkpointsRepository;
        this.runLessonsRepository = runLessonsRepository;
        this.stepsRepository = stepsRepository;
        this.lessonPagesRepository = lessonPagesRepository;
        this.questionVersionsRepository = questionVersionsRepository;
        this.studentsRepository = studentsRepository;
        this.studentMissionsService = studentMissionsService;
        this.assessmentsService = assessmentsService;
        this.missionProgressService = missionProgressService;
        this.livesService = livesService;
        this.hintsService = hintsService;
        this.notebookService = notebookService;
        this.entityManager = entityManager;
    }

    // ------------------------------------------------------------------ Avvio e lettura

    @Override
    @Transactional
    public MissionPlayDTO start(long studentId, String courseSlug, String missionSlug) {
        Mission mission = studentMissionsService.findPlayableMission(studentId, courseSlug, missionSlug);
        MissionRun run = runsRepository.findByMission(studentId, mission.getMissionId(), OPEN_RUN).stream()
                .findFirst().orElse(null);
        if (run == null) {
            ensureNoOtherInSubject(studentId, mission.getCourse().getSubject().getSubjectId(), null);
            requireLife(studentId);
            run = runsRepository.save(MissionRun.builder()
                    .student(studentsRepository.getReferenceById(studentId))
                    .mission(mission)
                    .subject(mission.getCourse().getSubject())
                    .status(MissionRunStatus.in_progress)
                    .build());
            return state(newAttempt(run, 1));
        }
        MissionAttempt last = missionAttemptsRepository
                .findFirstByMissionRun_MissionRunIdOrderByAttemptNumberDesc(run.getMissionRunId()).orElse(null);
        if (last != null && last.getStatus() == MissionAttemptStatus.pending_review) {
            throw new ConflictException("Hai consegnato l'ultimo esercizio: la missione aspetta la correzione della docente");
        }
        if (run.getStatus() == MissionRunStatus.pending_review) {
            // Dopo l'esito della docente il percorso torna in corso (vedi onTeacherAssessed)
            ensureNoOtherInSubject(studentId, run.getSubject().getSubjectId(), run.getMissionRunId());
            run.setStatus(MissionRunStatus.in_progress);
        }
        if (last != null && (last.getStatus() == MissionAttemptStatus.active || last.getStatus() == MissionAttemptStatus.saved)) {
            return doResume(last);
        }
        // Ritentativo dopo un errore: stesso percorso, con le lezioni lette e i checkpoint (§9.2)
        requireLife(studentId);
        return state(newAttempt(run, last == null ? 1 : last.getAttemptNumber() + 1));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CurrentMissionDTO> getCurrent(long studentId) {
        List<CurrentMissionDTO> current = new ArrayList<>();
        for (MissionRun run : runsRepository.findByStudent_StudentIdAndStatusInOrderByMissionRunId(studentId, OPEN_RUN)) {
            missionAttemptsRepository.findFirstByMissionRun_MissionRunIdOrderByAttemptNumberDesc(run.getMissionRunId())
                    .ifPresent(attempt -> current.add(new CurrentMissionDTO(attempt.getMissionAttemptId(), attempt.getStatus(),
                            run.getSubject().getSubjectName(), run.getMission().getCourse().getSlug(),
                            run.getMission().getSlug(), run.getMission().getMissionTitle())));
        }
        return current;
    }

    @Override
    @Transactional
    public MissionPlayDTO getState(long studentId, long missionAttemptId) {
        MissionAttempt attempt = findMissionAttempt(studentId, missionAttemptId);
        Snapshot snapshot = snapshot(attempt);
        refreshTimer(attempt, snapshot.open(), 0, true);
        return state(attempt);
    }

    // ------------------------------------------------------------------ Lezioni

    @Override
    @Transactional
    public MissionStepOutcomeDTO completeLesson(long studentId, long missionAttemptId, long lessonId) {
        MissionAttempt attempt = findMissionAttempt(studentId, missionAttemptId);
        requireActive(attempt);
        Snapshot snapshot = snapshot(attempt);
        if (snapshot.readLessons().contains(lessonId)) {
            return outcome(attempt, "lesson_read", null, false, null, List.of());
        }
        MissionStep current = snapshot.current();
        if (current == null || current.isExercise() || !current.getLesson().getLessonId().equals(lessonId)) {
            throw new ConflictException("Prima completa il passo attuale della missione");
        }
        MissionRun run = attempt.getMissionRun();
        entityManager.persist(MissionRunLesson.builder()
                .id(new MissionRunLesson.MissionRunLessonId(run.getMissionRunId(), lessonId))
                .missionRun(run).lesson(current.getLesson()).build());
        notebookService.acquireLesson(studentId, lessonId);
        MissionPassedDTO passed = advance(attempt);
        return outcome(attempt, passed != null ? "mission_passed" : "lesson_read", null, false, passed,
                passed != null ? passed.newStages() : List.of());
    }

    // ------------------------------------------------------------------ Esercizio: tempo, pausa, consegna

    @Override
    @Transactional
    public MissionSyncDTO sync(long studentId, long exerciseAttemptId, AnswerDTO body) {
        ExerciseAttempt exercise = findMissionExercise(studentId, exerciseAttemptId);
        MissionAttempt attempt = exercise.getMissionAttempt();
        if (exercise.getStatus() != ExerciseAttemptStatus.active || attempt.getStatus() != MissionAttemptStatus.active) {
            return new MissionSyncDTO(attempt.getStatus(), exercise.getRemainingTimeMs());
        }
        assessmentsService.validateAnswer(exercise, body, false);
        exercise.setResponse(AttemptJson.writeAnswer(body));
        TimerState timer = refreshTimer(attempt, exercise, 0, true);
        if (attempt.getStatus() == MissionAttemptStatus.active) {
            // Nuovo segno: il tempo continua a scorrere da qui
            exercise.setRemainingTimeMs(timer.remainingMs());
            exercise.setTimerStartedAt(OffsetDateTime.now());
        }
        return new MissionSyncDTO(attempt.getStatus(), exercise.getRemainingTimeMs());
    }

    @Override
    @Transactional
    public MissionPlayDTO pause(long studentId, long missionAttemptId, AnswerDTO body) {
        MissionAttempt attempt = findMissionAttempt(studentId, missionAttemptId);
        // Già in pausa, in attesa della docente o concluso: niente da salvare (la chiusura della pagina può
        // arrivare in qualunque momento)
        if (attempt.getStatus() != MissionAttemptStatus.active) {
            return state(attempt);
        }
        ExerciseAttempt exercise = snapshot(attempt).open();
        boolean editable = exercise != null
                && (exercise.getStatus() == ExerciseAttemptStatus.active || exercise.getStatus() == ExerciseAttemptStatus.saved);
        if (editable && body != null) {
            assessmentsService.validateAnswer(exercise, body, false);
        }
        TimerState timer = refreshTimer(attempt, exercise, 0, true);
        if (attempt.getStatus() == MissionAttemptStatus.failed) {
            return state(attempt);
        }
        if (editable && body != null) {
            exercise.setResponse(AttemptJson.writeAnswer(body));
        }
        if (exercise != null && exercise.getStatus() == ExerciseAttemptStatus.active) {
            stopTimer(exercise, timer);
            exercise.setStatus(ExerciseAttemptStatus.saved);
        }
        attempt.setStatus(MissionAttemptStatus.saved);
        return state(attempt);
    }

    @Override
    @Transactional
    public MissionPlayDTO resume(long studentId, long missionAttemptId) {
        MissionAttempt attempt = findMissionAttempt(studentId, missionAttemptId);
        if (attempt.getStatus() == MissionAttemptStatus.active) {
            return state(attempt);
        }
        if (attempt.getStatus() != MissionAttemptStatus.saved) {
            throw new ConflictException(attempt.getStatus() == MissionAttemptStatus.pending_review
                    ? "La missione aspetta la correzione della docente"
                    : "Questo tentativo è concluso: riparti dalla pagina della missione");
        }
        return doResume(attempt);
    }

    @Override
    @Transactional
    public MissionStepOutcomeDTO submitExercise(long studentId, long exerciseAttemptId, AnswerDTO body) {
        ExerciseAttempt exercise = findMissionExercise(studentId, exerciseAttemptId);
        MissionAttempt attempt = exercise.getMissionAttempt();
        // Doppio invio: stesso esito, nessun doppio effetto
        if (exercise.getStatus() == ExerciseAttemptStatus.assessed) {
            ExerciseAssessment done = assessmentsRepository.findById(exerciseAttemptId).orElseThrow();
            ExerciseOutcomeDTO outcome = assessmentsService.toOutcome(exercise, done);
            return outcome(attempt, outcome.passed() ? "passed" : "failed", outcome, false, null, List.of());
        }
        if (exercise.getStatus() == ExerciseAttemptStatus.pending_review) {
            return outcome(attempt, "pending_review", null, false, null, List.of());
        }
        if (exercise.getStatus() != ExerciseAttemptStatus.active || attempt.getStatus() != MissionAttemptStatus.active) {
            throw new ConflictException(attempt.getStatus() == MissionAttemptStatus.saved
                    ? "La missione è in pausa: premi \"Riprendi\" per consegnare"
                    : "Questo esercizio è già chiuso: riparti dalla pagina della missione");
        }
        assessmentsService.validateAnswer(exercise, body, true);
        // Esercizio ritirato o nascosto mentre lo studente lo svolgeva: saltato, nessuna vita persa (§6)
        Snapshot snapshot = snapshot(attempt);
        if (snapshot.open() != exercise) {
            exercise.setResponse(AttemptJson.writeAnswer(body));
            MissionPassedDTO passed = advance(attempt);
            return outcome(attempt, passed != null ? "mission_passed" : "withdrawn", null, false, passed,
                    passed != null ? passed.newStages() : List.of());
        }
        exercise.setResponse(AttemptJson.writeAnswer(body));
        TimerState timer = refreshTimer(attempt, exercise, SUBMIT_GRACE_MS, false);
        if (attempt.getStatus() == MissionAttemptStatus.failed) {
            return outcome(attempt, "expired", null, true, null, List.of());
        }
        stopTimer(exercise, timer);
        exercise.setSubmittedAt(OffsetDateTime.now());
        if (!assessmentsService.isAutomatic(exercise)) {
            // Consegna alla docente: tempo fermo, attesa senza perdere vite, niente "Abbandona" (§1.4)
            exercise.setStatus(ExerciseAttemptStatus.pending_review);
            attempt.setStatus(MissionAttemptStatus.pending_review);
            attempt.getMissionRun().setStatus(MissionRunStatus.pending_review);
            return outcome(attempt, "pending_review", null, false, null, List.of());
        }
        ExerciseAssessment assessment = assessmentsService.assessAutomatically(exercise);
        return afterAssessment(studentId, attempt, exercise, assessment);
    }

    @Override
    @Transactional
    public MissionPlayDTO cancelSubmission(long studentId, long exerciseAttemptId) {
        // Riga bloccata: se la docente sta salvando l'esito, si aspetta e poi si vede che è già corretto
        ExerciseAttempt exercise = exerciseAttemptsRepository.lockById(exerciseAttemptId)
                .filter(attempt -> attempt.getStudent().getStudentId().equals(studentId)
                        && attempt.getContext() == ExerciseContext.mission)
                .orElseThrow(() -> new NotFoundException("Esercizio non trovato"));
        if (exercise.getStatus() == ExerciseAttemptStatus.assessed || assessmentsRepository.existsById(exerciseAttemptId)) {
            throw new ConflictException("La docente ha già corretto questo esercizio: l'esito è definitivo");
        }
        if (exercise.getStatus() != ExerciseAttemptStatus.pending_review) {
            throw new ConflictException("Non c'è un invio da annullare");
        }
        MissionAttempt attempt = exercise.getMissionAttempt();
        MissionRun run = attempt.getMissionRun();
        // Il percorso torna in corso: non deve essercene già un altro nella stessa materia (§9)
        ensureNoOtherInSubject(studentId, run.getSubject().getSubjectId(), run.getMissionRunId());
        // Risposta modificabile e tempo com'era al momento dell'invio; nessuna vita persa (§2)
        exercise.setStatus(ExerciseAttemptStatus.saved);
        exercise.setSubmittedAt(null);
        attempt.setStatus(MissionAttemptStatus.saved);
        run.setStatus(MissionRunStatus.in_progress);
        return state(attempt);
    }

    @Override
    @Transactional
    public LivesDTO abandon(long studentId, long missionAttemptId) {
        MissionAttempt attempt = findMissionAttempt(studentId, missionAttemptId);
        MissionRun run = attempt.getMissionRun();
        switch (attempt.getStatus()) {
            case pending_review -> throw new ConflictException(
                    "Hai consegnato l'ultimo esercizio alla docente: puoi annullare l'invio, ma non abbandonare la missione");
            case passed -> throw new ConflictException("Questa missione è già superata");
            case failed -> {
                // Tentativo già fallito (e vita già persa): si chiude solo il percorso
                boolean last = missionAttemptsRepository
                        .findFirstByMissionRun_MissionRunIdOrderByAttemptNumberDesc(run.getMissionRunId())
                        .map(a -> a.getMissionAttemptId().equals(attempt.getMissionAttemptId())).orElse(false);
                if (last && OPEN_RUN.contains(run.getStatus())) {
                    run.setStatus(MissionRunStatus.abandoned);
                }
                return livesService.getLives(studentId);
            }
            default -> {
                ExerciseAttempt exercise = exerciseAttemptsRepository
                        .findFirstByMissionAttempt_MissionAttemptIdAndStatusIn(attempt.getMissionAttemptId(), OPEN_EXERCISE)
                        .orElse(null);
                if (exercise != null) {
                    close(exercise);
                }
                run.setStatus(MissionRunStatus.abandoned);
                // Si riparte da zero con un percorso nuovo (§9.2)
                return fail(attempt, FailureReason.abandonment);
            }
        }
    }

    @Override
    @Transactional
    public MissionStepOutcomeDTO onTeacherAssessed(long exerciseAttemptId) {
        ExerciseAttempt exercise = exerciseAttemptsRepository.findById(exerciseAttemptId)
                .filter(e -> e.getContext() == ExerciseContext.mission)
                .orElseThrow(() -> new NotFoundException("Tentativo " + exerciseAttemptId + " non trovato"));
        ExerciseAssessment assessment = assessmentsRepository.findById(exerciseAttemptId)
                .orElseThrow(() -> new NotFoundException("Il tentativo " + exerciseAttemptId + " non ha ancora un esito"));
        MissionAttempt attempt = exercise.getMissionAttempt();
        long studentId = exercise.getStudent().getStudentId();
        if (attempt.getStatus() != MissionAttemptStatus.pending_review) {
            return outcome(attempt, assessment.getPercentage().compareTo(assessment.getPassingThreshold()) >= 0
                    ? "passed" : "failed", assessmentsService.toOutcome(exercise, assessment), false, null, List.of());
        }
        if (exercise.getStatus() != ExerciseAttemptStatus.assessed) {
            exercise.setStatus(ExerciseAttemptStatus.assessed);
            exercise.setCompletedAt(OffsetDateTime.now());
        }
        // Il percorso torna in corso per proseguire o ritentare; se intanto lo studente ha aperto un'altra
        // missione della stessa materia resta in attesa e start() lo riapre (§9)
        MissionRun run = attempt.getMissionRun();
        boolean otherOpen = runsRepository.findFirstByStudent_StudentIdAndSubject_SubjectIdAndStatus(studentId,
                        run.getSubject().getSubjectId(), MissionRunStatus.in_progress)
                .filter(other -> !other.getMissionRunId().equals(run.getMissionRunId())).isPresent();
        // Lo studente non è davanti alla missione: se prosegue, riparte da "Riprendi" e il tempo del passo dopo
        // parte solo allora
        attempt.setStatus(MissionAttemptStatus.saved);
        if (!otherOpen) {
            run.setStatus(MissionRunStatus.in_progress);
        }
        return afterAssessment(studentId, attempt, exercise, assessment);
    }

    // ------------------------------------------------------------------ Passi comuni

    // Dopo un giudizio: punti skill sempre (§5); superato -> checkpoint e passo successivo; altrimenti tentativo fallito
    private MissionStepOutcomeDTO afterAssessment(long studentId, MissionAttempt attempt, ExerciseAttempt exercise,
                                                  ExerciseAssessment assessment) {
        SkillPointsDTO points = missionProgressService.onExerciseAssessed(studentId, exercise.getExerciseAttemptId());
        ExerciseOutcomeDTO outcome = assessmentsService.toOutcome(exercise, assessment).withRewards(points.earned(), null);
        List<Integer> stages = new ArrayList<>(points.newStages());
        if (!outcome.passed()) {
            fail(attempt, FailureReason.below_threshold);
            // Si riparte da quell'esercizio con risposta vuota e tempo completo, nel prossimo tentativo (§9.2)
            return outcome(attempt, "failed", outcome, true, null, stages);
        }
        MissionRun run = attempt.getMissionRun();
        entityManager.persist(MissionCheckpoint.builder()
                .id(new MissionCheckpoint.MissionCheckpointId(run.getMissionRunId(),
                        exercise.getMissionExercise().getMissionExerciseId()))
                .missionRun(run).missionExercise(exercise.getMissionExercise())
                .exerciseAssessment(assessment).missionAttempt(attempt)
                .build());
        MissionPassedDTO passed = advance(attempt);
        if (passed != null) {
            stages.addAll(passed.newStages());
        }
        return outcome(attempt, passed != null ? "mission_passed" : "passed", outcome, false, passed, stages);
    }

    // Dopo un passo fatto: tutti i passi fatti -> missione superata; altrimenti apre l'esercizio attuale
    private MissionPassedDTO advance(MissionAttempt attempt) {
        entityManager.flush();
        Snapshot snapshot = snapshot(attempt);
        if (snapshot.position().complete()) {
            return pass(attempt);
        }
        ensureOpen(attempt, snapshot);
        return null;
    }

    private MissionPassedDTO pass(MissionAttempt attempt) {
        attempt.setStatus(MissionAttemptStatus.passed);
        attempt.setCompletedAt(OffsetDateTime.now());
        attempt.getMissionRun().setStatus(MissionRunStatus.passed);
        entityManager.flush();
        return missionProgressService.onMissionPassed(attempt.getStudent().getStudentId(), attempt.getMissionAttemptId());
    }

    // Una sola vita per tentativo fallito (ILivesService la conta una volta sola)
    private LivesDTO fail(MissionAttempt attempt, FailureReason reason) {
        attempt.setStatus(MissionAttemptStatus.failed);
        attempt.setFailureReason(reason);
        attempt.setCompletedAt(OffsetDateTime.now());
        return livesService.onMissionAttemptFailed(attempt.getStudent().getStudentId(), attempt.getMissionAttemptId());
    }

    private MissionPlayDTO doResume(MissionAttempt attempt) {
        if (attempt.getStatus() == MissionAttemptStatus.saved) {
            requireLife(attempt.getStudent().getStudentId());
            attempt.setStatus(MissionAttemptStatus.active);
            ExerciseAttempt exercise = snapshot(attempt).open();
            if (exercise != null && exercise.getStatus() == ExerciseAttemptStatus.saved) {
                // Stesso punto, stessa risposta, tempo residuo: il tempo riparte adesso
                exercise.setStatus(ExerciseAttemptStatus.active);
                exercise.setTimerStartedAt(OffsetDateTime.now());
            }
        }
        return state(attempt);
    }

    private MissionAttempt newAttempt(MissionRun run, int number) {
        return missionAttemptsRepository.save(MissionAttempt.builder()
                .missionRun(run)
                .student(run.getStudent())
                .attemptNumber(number)
                .status(MissionAttemptStatus.active)
                .build());
    }

    // Applica le scadenze dell'esercizio aperto. Tempo finito -> tentativo fallito (una vita). Collegamento
    // interrotto oltre la tolleranza -> missione in pausa, se pauseIfDisconnected. Restituisce il tempo di adesso,
    // null se il tempo non sta scorrendo
    private TimerState refreshTimer(MissionAttempt attempt, ExerciseAttempt exercise, long graceMs, boolean pauseIfDisconnected) {
        if (attempt.getStatus() != MissionAttemptStatus.active || exercise == null
                || exercise.getStatus() != ExerciseAttemptStatus.active) {
            return null;
        }
        if (exercise.getTimerStartedAt() == null) {
            exercise.setTimerStartedAt(OffsetDateTime.now());
        }
        TimerState timer = PlayRules.timer(exercise.getRemainingTimeMs(), exercise.getTimerStartedAt(),
                OffsetDateTime.now().minusNanos(graceMs * 1_000_000), TOLERANCE_MS);
        if (timer.expired()) {
            exercise.setStatus(ExerciseAttemptStatus.expired);
            exercise.setRemainingTimeMs(0L);
            exercise.setTimerStartedAt(null);
            exercise.setCompletedAt(OffsetDateTime.now());
            fail(attempt, FailureReason.time_expired);
        } else if (timer.disconnected() && pauseIfDisconnected) {
            stopTimer(exercise, timer);
            exercise.setStatus(ExerciseAttemptStatus.saved);
            attempt.setStatus(MissionAttemptStatus.saved);
        }
        return timer;
    }

    private static void stopTimer(ExerciseAttempt exercise, TimerState timer) {
        if (timer != null) {
            exercise.setRemainingTimeMs(timer.remainingMs());
        }
        exercise.setTimerStartedAt(null);
    }

    private static void close(ExerciseAttempt exercise) {
        exercise.setStatus(ExerciseAttemptStatus.abandoned);
        exercise.setTimerStartedAt(null);
        exercise.setCompletedAt(OffsetDateTime.now());
    }

    // ------------------------------------------------------------------ Dove si trova lo studente

    private record Snapshot(List<MissionStep> steps, List<Long> readLessons, List<Long> passedExercises,
                            MissionPath.Position position, MissionStep current, ExerciseAttempt open) {
    }

    // Passi visibili, lezioni lette, checkpoint, passo attuale ed esercizio aperto. Un esercizio aperto che non è
    // più il passo attuale (ritirato o nascosto dall'autrice) si chiude senza esito (§6); una consegna già in
    // attesa della docente resta valida
    private Snapshot snapshot(MissionAttempt attempt) {
        MissionRun run = attempt.getMissionRun();
        List<MissionStep> steps = StudentVisibility.visibleSteps(stepsRepository.findByMissionOrdered(run.getMission().getMissionId()));
        List<Long> readLessons = runLessonsRepository.findLessonIds(run.getMissionRunId());
        List<Long> passedExercises = checkpointsRepository.findExerciseIds(run.getMissionRunId());
        MissionPath.Position position = MissionPath.of(steps.stream()
                .map(step -> new MissionPath.Step(step.isExercise(),
                        step.isExercise() ? step.getMissionExercise().getMissionExerciseId() : step.getLesson().getLessonId()))
                .toList(), readLessons, passedExercises);
        MissionStep current = position.complete() ? null : steps.get(position.currentIndex());
        ExerciseAttempt open = exerciseAttemptsRepository
                .findFirstByMissionAttempt_MissionAttemptIdAndStatusIn(attempt.getMissionAttemptId(), OPEN_EXERCISE)
                .orElse(null);
        if (open != null && open.getStatus() != ExerciseAttemptStatus.pending_review && !isCurrent(open, current)) {
            close(open);
            open = null;
        }
        return new Snapshot(steps, readLessons, passedExercises, position, current, open);
    }

    private static boolean isCurrent(ExerciseAttempt exercise, MissionStep current) {
        return current != null && current.isExercise()
                && current.getMissionExercise().getMissionExerciseId().equals(exercise.getMissionExercise().getMissionExerciseId());
    }

    // Il passo attuale è un esercizio non ancora aperto: compare adesso e il tempo parte (§9.1)
    private ExerciseAttempt ensureOpen(MissionAttempt attempt, Snapshot snapshot) {
        if (attempt.getStatus() != MissionAttemptStatus.active || snapshot.open() != null
                || snapshot.current() == null || !snapshot.current().isExercise()) {
            return snapshot.open();
        }
        MissionExercise item = snapshot.current().getMissionExercise();
        Long entryId = item.getQuestionBankEntry().getQuestionBankEntryId();
        QuestionVersion version = questionVersionsRepository.findPublished(entryId)
                .orElseThrow(() -> new NotFoundException("L'esercizio non è disponibile in questo momento: riprova più tardi"));
        return exerciseAttemptsRepository.save(ExerciseAttempt.builder()
                .student(attempt.getStudent())
                .context(ExerciseContext.mission)
                .questionBankEntry(item.getQuestionBankEntry())
                .questionVersion(version)
                .missionAttempt(attempt)
                .missionExercise(item)
                .status(ExerciseAttemptStatus.active)
                .configurationSnapshot(AttemptJson.writeConfiguration("missionExerciseId", item.getMissionExerciseId(),
                        item.getMaxPoints(), assessmentsService.newAnswerOrder(version.getQuestionVersionId())))
                .allowedDurationMs(item.getAllowedDurationMs())
                .remainingTimeMs(item.getAllowedDurationMs())
                .timerStartedAt(OffsetDateTime.now())
                .build());
    }

    // Stato da mostrare. Se tutti i passi visibili sono fatti (per esempio l'ultimo esercizio è stato ritirato)
    // la missione risulta superata (§6); se il passo attuale è un esercizio lo apre
    private MissionPlayDTO state(MissionAttempt attempt) {
        Snapshot snapshot = snapshot(attempt);
        boolean playing = attempt.getStatus() == MissionAttemptStatus.active || attempt.getStatus() == MissionAttemptStatus.saved;
        if (playing && snapshot.position().complete() && snapshot.open() == null) {
            pass(attempt);
            snapshot = snapshot(attempt);
        }
        ExerciseAttempt open = ensureOpen(attempt, snapshot);
        MissionRun run = attempt.getMissionRun();
        long studentId = attempt.getStudent().getStudentId();
        boolean finished = attempt.getStatus() == MissionAttemptStatus.passed;

        LessonPlayDTO lesson = null;
        MissionStep current = snapshot.current();
        if (attempt.getStatus() == MissionAttemptStatus.active && current != null && !current.isExercise()) {
            Lesson item = current.getLesson();
            lesson = new LessonPlayDTO(item.getLessonId(), item.getLessonTitle(),
                    lessonPagesRepository.findByLesson_LessonIdOrderByLessonPageSortOrder(item.getLessonId()).stream()
                            .map(LessonPageResponseDTO::from).toList());
        }
        MissionExercisePlayDTO exercise = open == null ? null : toExercise(open);
        return new MissionPlayDTO(run.getMissionRunId(), attempt.getMissionAttemptId(), attempt.getAttemptNumber(),
                run.getMission().getCourse().getSlug(), run.getMission().getSlug(), run.getMission().getMissionTitle(),
                run.getSubject().getSubjectName(), attempt.getStatus(), attempt.getFailureReason(),
                studentMissionsService.stepper(snapshot.steps(), snapshot.readLessons(), snapshot.passedExercises()),
                finished || snapshot.position().complete() ? null : snapshot.position().currentIndex() + 1,
                lesson, exercise, livesService.getLives(studentId), hintsService.getCharges(studentId));
    }

    private MissionExercisePlayDTO toExercise(ExerciseAttempt open) {
        boolean running = open.getStatus() == ExerciseAttemptStatus.active && open.getTimerStartedAt() != null;
        long remaining = running
                ? PlayRules.timer(open.getRemainingTimeMs(), open.getTimerStartedAt(), OffsetDateTime.now(), TOLERANCE_MS).remainingMs()
                : open.getRemainingTimeMs();
        return new MissionExercisePlayDTO(open.getExerciseAttemptId(), open.getMissionExercise().getMissionExerciseId(),
                open.getStatus(), assessmentsService.toView(open), AttemptJson.readAnswer(open.getResponse()),
                AttemptJson.maxPoints(open.getConfigurationSnapshot()), open.getAllowedDurationMs(), remaining, running);
    }

    private MissionStepOutcomeDTO outcome(MissionAttempt attempt, String result, ExerciseOutcomeDTO outcome, boolean lifeLost,
                                          MissionPassedDTO passed, List<Integer> stages) {
        MissionPlayDTO state = state(attempt);
        return new MissionStepOutcomeDTO(result, outcome, lifeLost, state.lives(), passed, stages, state);
    }

    // ------------------------------------------------------------------ Regole d'accesso

    private void requireActive(MissionAttempt attempt) {
        if (attempt.getStatus() == MissionAttemptStatus.saved) {
            throw new ConflictException("La missione è in pausa: premi \"Riprendi\" per continuare");
        }
        if (attempt.getStatus() != MissionAttemptStatus.active) {
            throw new ConflictException("Questo tentativo è concluso: riparti dalla pagina della missione");
        }
    }

    // Almeno una vita per avviare, ritentare o riprendere (§9.1)
    private void requireLife(long studentId) {
        if (livesService.canStartMission(studentId)) {
            return;
        }
        LivesDTO lives = livesService.getLives(studentId);
        String when = lives.rechargeAt() == null ? ""
                : " Le vite tornano il " + lives.rechargeAt().atZoneSameInstant(ROME).format(RECHARGE_FORMAT) + ".";
        throw new ConflictException("Non hai vite per giocare la missione." + when + " Intanto puoi allenarti in Palestra");
    }

    // Una missione in corso per materia (§9). exceptRunId: il percorso che sta per tornare in corso
    private void ensureNoOtherInSubject(long studentId, long subjectId, Long exceptRunId) {
        runsRepository.findFirstByStudent_StudentIdAndSubject_SubjectIdAndStatus(studentId, subjectId, MissionRunStatus.in_progress)
                .filter(other -> !other.getMissionRunId().equals(exceptRunId))
                .ifPresent(other -> {
                    throw new ConflictException("Hai già una missione di " + other.getSubject().getSubjectName()
                            + " in corso: \"" + other.getMission().getMissionTitle()
                            + "\". Concludila o abbandonala prima di iniziarne un'altra");
                });
    }

    // Un tentativo di un altro studente è come se non esistesse
    private MissionAttempt findMissionAttempt(long studentId, long missionAttemptId) {
        return missionAttemptsRepository.findById(missionAttemptId)
                .filter(attempt -> attempt.getStudent().getStudentId().equals(studentId))
                .orElseThrow(() -> new NotFoundException("Missione non trovata"));
    }

    private ExerciseAttempt findMissionExercise(long studentId, long exerciseAttemptId) {
        return exerciseAttemptsRepository.findById(exerciseAttemptId)
                .filter(attempt -> attempt.getStudent().getStudentId().equals(studentId)
                        && attempt.getContext() == ExerciseContext.mission)
                .orElseThrow(() -> new NotFoundException("Esercizio non trovato"));
    }
}
