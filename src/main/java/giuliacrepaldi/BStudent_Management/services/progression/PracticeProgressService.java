package giuliacrepaldi.BStudent_Management.services.progression;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.progression.PracticeRecord;
import giuliacrepaldi.BStudent_Management.enums.progression.SourceKind;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeRewardsDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.PracticeRecordsRepository;
import giuliacrepaldi.BStudent_Management.services.assessment.interfaces.IAssessmentsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IDailyGoalsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IPracticeProgressService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.ISkillCreditsService;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PracticeProgressService implements IPracticeProgressService {

    private final ExerciseAssessmentsRepository assessmentsRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;
    private final PracticeRecordsRepository recordsRepository;
    private final StudentsRepository studentsRepository;
    private final IAssessmentsService assessmentsService;
    private final ISkillCreditsService skillCreditsService;
    private final IDailyGoalsService dailyGoalsService;
    private final IStudentXpService studentXpService;
    private final EntityManager entityManager;

    public PracticeProgressService(ExerciseAssessmentsRepository assessmentsRepository,
                                   PracticeExercisesRepository practiceExercisesRepository,
                                   PracticeRecordsRepository recordsRepository, StudentsRepository studentsRepository,
                                   IAssessmentsService assessmentsService, ISkillCreditsService skillCreditsService,
                                   IDailyGoalsService dailyGoalsService, IStudentXpService studentXpService,
                                   EntityManager entityManager) {
        this.assessmentsRepository = assessmentsRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
        this.recordsRepository = recordsRepository;
        this.studentsRepository = studentsRepository;
        this.assessmentsService = assessmentsService;
        this.skillCreditsService = skillCreditsService;
        this.dailyGoalsService = dailyGoalsService;
        this.studentXpService = studentXpService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ExerciseOutcomeDTO onAssessed(long studentId, long exerciseAttemptId) {
        ExerciseAssessment assessment = assessmentsRepository.findById(exerciseAttemptId)
                .orElseThrow(() -> new NotFoundException("Il tentativo " + exerciseAttemptId + " non ha ancora un esito"));
        ExerciseAttempt attempt = assessment.getExerciseAttempt();
        PracticeExercise exercise = practiceExercisesRepository
                .findByQuestionBankEntry_QuestionBankEntryId(attempt.getQuestionBankEntry().getQuestionBankEntryId())
                .orElseThrow(() -> new NotFoundException("Voce della Palestra non trovata"));
        // I punti massimi sono quelli del momento in cui il tentativo è partito (§6.1)
        BigDecimal maxPoints = AttemptJson.maxPoints(attempt.getConfigurationSnapshot());
        if (maxPoints == null) {
            maxPoints = exercise.getMaxPoints();
        }
        BigDecimal contribution = PlayRules.practiceContribution(maxPoints, assessment.getPercentage(),
                assessment.getPassingThreshold());
        BigDecimal earned = PlayRules.ZERO;
        if (contribution.signum() > 0) {
            updateRecord(studentId, exercise, assessment, maxPoints, contribution);
            // Solo la differenza rispetto al record: ripetere non accumula punti all'infinito (§10.1)
            earned = skillCreditsService.creditImprovement(studentId, exercise.getTopic().getSubject().getSubjectId(),
                    exercise.getSkill().getSkillId(), SourceKind.practice, exercise.getPracticeExerciseId(), contribution,
                    "practice:" + exerciseAttemptId);
        }
        // Serve che il giudizio sia già visibile alle letture native della sfida e del tris
        entityManager.flush();
        PracticeRewardsDTO rewards = dailyGoalsService.onPracticeAttemptEvaluated(studentId, exerciseAttemptId);
        if (earned.signum() > 0) {
            studentXpService.onSkillPointsCredited(studentId);
        }
        return assessmentsService.toOutcome(attempt, assessment).withRewards(earned, rewards);
    }

    private void updateRecord(long studentId, PracticeExercise exercise, ExerciseAssessment assessment,
                              BigDecimal maxPoints, BigDecimal contribution) {
        PracticeRecord.PracticeRecordId id = new PracticeRecord.PracticeRecordId(studentId, exercise.getPracticeExerciseId());
        PracticeRecord record = recordsRepository.findById(id).orElse(null);
        if (record == null) {
            // persist e non save: con la chiave composta già valorizzata save farebbe un merge
            entityManager.persist(PracticeRecord.builder().id(id)
                    .student(studentsRepository.getReferenceById(studentId))
                    .practiceExercise(exercise).skill(exercise.getSkill()).exerciseAssessment(assessment)
                    .percentage(assessment.getPercentage()).maxPointsSnapshot(maxPoints).points(contribution).build());
        } else if (contribution.compareTo(record.getPoints()) > 0) {
            // Un risultato uguale o peggiore non tocca il record
            record.setExerciseAssessment(assessment);
            record.setSkill(exercise.getSkill());
            record.setPercentage(assessment.getPercentage());
            record.setMaxPointsSnapshot(maxPoints);
            record.setPoints(contribution);
        }
    }
}
