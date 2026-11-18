package giuliacrepaldi.BStudent_Management.services.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.ForbiddenException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.PracticeAttemptDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeLockDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionVersionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.services.assessment.interfaces.IAssessmentsService;
import giuliacrepaldi.BStudent_Management.services.attempts.interfaces.IPracticeAttemptsService;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IPracticeProgressService;
import giuliacrepaldi.BStudent_Management.services.student.StudentVisibility;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PracticeAttemptsService implements IPracticeAttemptsService {

    private static final List<ExerciseAttemptStatus> OPEN =
            List.of(ExerciseAttemptStatus.active, ExerciseAttemptStatus.saved, ExerciseAttemptStatus.pending_review);

    private final ExerciseAttemptsRepository attemptsRepository;
    private final ExerciseAssessmentsRepository assessmentsRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;
    private final QuestionVersionsRepository questionVersionsRepository;
    private final StudentsRepository studentsRepository;
    private final IAssessmentsService assessmentsService;
    private final IPracticeProgressService practiceProgressService;
    private final ISkillLevelsService skillLevelsService;
    private final IEntitlementService entitlementService;

    public PracticeAttemptsService(ExerciseAttemptsRepository attemptsRepository,
                                   ExerciseAssessmentsRepository assessmentsRepository,
                                   PracticeExercisesRepository practiceExercisesRepository,
                                   QuestionVersionsRepository questionVersionsRepository,
                                   StudentsRepository studentsRepository, IAssessmentsService assessmentsService,
                                   IPracticeProgressService practiceProgressService,
                                   ISkillLevelsService skillLevelsService, IEntitlementService entitlementService) {
        this.attemptsRepository = attemptsRepository;
        this.assessmentsRepository = assessmentsRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
        this.questionVersionsRepository = questionVersionsRepository;
        this.studentsRepository = studentsRepository;
        this.assessmentsService = assessmentsService;
        this.practiceProgressService = practiceProgressService;
        this.skillLevelsService = skillLevelsService;
        this.entitlementService = entitlementService;
    }

    @Override
    @Transactional
    public PracticeAttemptDTO start(long studentId, long practiceExerciseId) {
        PracticeExercise exercise = practiceExercisesRepository.findById(practiceExerciseId)
                .filter(StudentVisibility::isVisible)
                .orElseThrow(() -> new NotFoundException("Esercizio non trovato"));
        QuestionBankEntry entry = exercise.getQuestionBankEntry();
        ExerciseAttempt open = attemptsRepository.findOpenPractice(studentId, entry.getQuestionBankEntryId(), OPEN).orElse(null);
        if (open != null) {
            if (open.getStatus() == ExerciseAttemptStatus.pending_review) {
                throw new ConflictException("Questo esercizio è in correzione: intanto puoi farne altri");
            }
            // Riprende la bozza: una sola per esercizio (§7.2)
            open.setStatus(ExerciseAttemptStatus.active);
            return toDTO(open, exercise.getPracticeExerciseId(), null);
        }
        // I due lucchetti valgono anche per chi conosce l'indirizzo dell'esercizio
        PracticeLockDTO lock = skillLevelsService.getLock(studentId, practiceExerciseId);
        if (lock.locked()) {
            throw new ConflictException(lock.message());
        }
        if (exercise.getAccessTier() == AccessTier.premium && !entitlementService.hasPremium(studentId)) {
            throw new ForbiddenException("Questo esercizio fa parte dell'abbonamento");
        }
        // Lo studente svolge solo la versione pubblicata; il tentativo la conserva (§6.1)
        QuestionVersion version = questionVersionsRepository.findPublished(entry.getQuestionBankEntryId())
                .orElseThrow(() -> new NotFoundException("Esercizio non trovato"));
        ExerciseAttempt attempt = attemptsRepository.save(ExerciseAttempt.builder()
                .student(studentsRepository.getReferenceById(studentId))
                .context(ExerciseContext.practice)
                .questionBankEntry(entry)
                .questionVersion(version)
                .status(ExerciseAttemptStatus.active)
                .configurationSnapshot(AttemptJson.writeConfiguration("practiceExerciseId", practiceExerciseId,
                        exercise.getMaxPoints(), assessmentsService.newAnswerOrder(version.getQuestionVersionId())))
                .build());
        return toDTO(attempt, practiceExerciseId, null);
    }

    @Override
    @Transactional
    public PracticeAttemptDTO getAttempt(long studentId, long exerciseAttemptId) {
        ExerciseAttempt attempt = findAttempt(studentId, exerciseAttemptId);
        closeIfWithdrawn(attempt);
        ExerciseOutcomeDTO outcome = assessmentsRepository.findById(exerciseAttemptId)
                .map(assessment -> assessmentsService.toOutcome(attempt, assessment)).orElse(null);
        return toDTO(attempt, practiceExerciseId(attempt), outcome);
    }

    @Override
    @Transactional
    public PracticeAttemptDTO saveDraft(long studentId, long exerciseAttemptId, AnswerDTO body) {
        ExerciseAttempt attempt = findEditable(studentId, exerciseAttemptId);
        assessmentsService.validateAnswer(attempt, body, false);
        attempt.setResponse(AttemptJson.writeAnswer(body));
        attempt.setStatus(ExerciseAttemptStatus.saved);
        return toDTO(attempt, practiceExerciseId(attempt), null);
    }

    @Override
    @Transactional
    public void leave(long studentId, long exerciseAttemptId) {
        ExerciseAttempt attempt = findAttempt(studentId, exerciseAttemptId);
        // Già chiuso: nessun effetto. Una consegna in correzione non si lascia: si annulla l'invio
        if (attempt.getStatus() == ExerciseAttemptStatus.active || attempt.getStatus() == ExerciseAttemptStatus.saved) {
            close(attempt);
        } else if (attempt.getStatus() == ExerciseAttemptStatus.pending_review) {
            throw new ConflictException("L'esercizio è in correzione: per riprenderlo usa \"Annulla invio\"");
        }
    }

    @Override
    @Transactional
    public PracticeAttemptDTO submit(long studentId, long exerciseAttemptId, AnswerDTO body) {
        ExerciseAttempt attempt = findAttempt(studentId, exerciseAttemptId);
        Long practiceExerciseId = practiceExerciseId(attempt);
        // Doppio invio: stesso risultato, nessun doppio effetto
        if (attempt.getStatus() == ExerciseAttemptStatus.assessed) {
            ExerciseAssessment done = assessmentsRepository.findById(exerciseAttemptId).orElseThrow();
            return toDTO(attempt, practiceExerciseId, assessmentsService.toOutcome(attempt, done));
        }
        if (attempt.getStatus() == ExerciseAttemptStatus.pending_review) {
            return toDTO(attempt, practiceExerciseId, null);
        }
        if (closeIfWithdrawn(attempt) || attempt.getStatus() == ExerciseAttemptStatus.abandoned) {
            throw new ConflictException("Questo esercizio non è più disponibile");
        }
        assessmentsService.validateAnswer(attempt, body, true);
        attempt.setResponse(AttemptJson.writeAnswer(body));
        attempt.setSubmittedAt(OffsetDateTime.now());
        if (!assessmentsService.isAutomatic(attempt)) {
            // Consegna alla docente: risposta bloccata fino all'esito; intanto si possono fare altri esercizi (§7.2)
            attempt.setStatus(ExerciseAttemptStatus.pending_review);
            return toDTO(attempt, practiceExerciseId, null);
        }
        assessmentsService.assessAutomatically(attempt);
        ExerciseOutcomeDTO outcome = practiceProgressService.onAssessed(studentId, exerciseAttemptId);
        return toDTO(attempt, practiceExerciseId, outcome);
    }

    @Override
    @Transactional
    public PracticeAttemptDTO cancelSubmission(long studentId, long exerciseAttemptId) {
        // Riga bloccata: se la docente sta salvando l'esito, si aspetta e poi si vede che è già corretto
        ExerciseAttempt attempt = attemptsRepository.lockById(exerciseAttemptId)
                .filter(a -> a.getStudent().getStudentId().equals(studentId) && a.getContext() == ExerciseContext.practice)
                .orElseThrow(() -> new NotFoundException("Tentativo non trovato"));
        if (attempt.getStatus() == ExerciseAttemptStatus.assessed) {
            throw new ConflictException("La docente ha già corretto questo esercizio: l'esito è definitivo");
        }
        if (attempt.getStatus() != ExerciseAttemptStatus.pending_review) {
            throw new ConflictException("Non c'è un invio da annullare");
        }
        // Lo studente ritrova la sua risposta, modificabile (Svolgimento §2, §7)
        attempt.setStatus(ExerciseAttemptStatus.saved);
        attempt.setSubmittedAt(null);
        return toDTO(attempt, practiceExerciseId(attempt), null);
    }

    // Esercizio ritirato dal banco con un tentativo aperto: si chiude senza esito (Svolgimento §6).
    // Una consegna già in correzione resta valida
    private boolean closeIfWithdrawn(ExerciseAttempt attempt) {
        boolean editable = attempt.getStatus() == ExerciseAttemptStatus.active || attempt.getStatus() == ExerciseAttemptStatus.saved;
        if (editable && attempt.getQuestionBankEntry().getStatus() == ContentStatus.withdrawn) {
            close(attempt);
            return true;
        }
        return false;
    }

    private static void close(ExerciseAttempt attempt) {
        attempt.setStatus(ExerciseAttemptStatus.abandoned);
        attempt.setCompletedAt(OffsetDateTime.now());
    }

    private ExerciseAttempt findEditable(long studentId, long exerciseAttemptId) {
        ExerciseAttempt attempt = findAttempt(studentId, exerciseAttemptId);
        if (closeIfWithdrawn(attempt)) {
            throw new ConflictException("Questo esercizio non è più disponibile");
        }
        if (attempt.getStatus() != ExerciseAttemptStatus.active && attempt.getStatus() != ExerciseAttemptStatus.saved) {
            throw new ConflictException(attempt.getStatus() == ExerciseAttemptStatus.pending_review
                    ? "L'esercizio è in correzione: la risposta non si può modificare"
                    : "Questo esercizio è già concluso: avviane uno nuovo");
        }
        return attempt;
    }

    // Un tentativo altrui, o di una missione, è come se non esistesse
    private ExerciseAttempt findAttempt(long studentId, long exerciseAttemptId) {
        return attemptsRepository.findById(exerciseAttemptId)
                .filter(attempt -> attempt.getStudent().getStudentId().equals(studentId)
                        && attempt.getContext() == ExerciseContext.practice)
                .orElseThrow(() -> new NotFoundException("Tentativo non trovato"));
    }

    private Long practiceExerciseId(ExerciseAttempt attempt) {
        return practiceExercisesRepository
                .findByQuestionBankEntry_QuestionBankEntryId(attempt.getQuestionBankEntry().getQuestionBankEntryId())
                .map(PracticeExercise::getPracticeExerciseId).orElse(null);
    }

    private PracticeAttemptDTO toDTO(ExerciseAttempt attempt, Long practiceExerciseId, ExerciseOutcomeDTO outcome) {
        return new PracticeAttemptDTO(attempt.getExerciseAttemptId(), practiceExerciseId, attempt.getStatus(),
                assessmentsService.toView(attempt), AttemptJson.readAnswer(attempt.getResponse()), outcome);
    }
}
