package giuliacrepaldi.BStudent_Management.services.progression;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionCheckpoint;
import giuliacrepaldi.BStudent_Management.entities.catalog.AcademySetting;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;
import giuliacrepaldi.BStudent_Management.entities.progression.MissionRecord;
import giuliacrepaldi.BStudent_Management.entities.progression.MissionSkillContribution;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.progression.SourceKind;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.progression.CourseResultDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.MissionPassedDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.SkillPointsDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionCheckpointsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.AcademySettingsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExerciseSkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.MissionRecordsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.MissionSkillContributionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.SkillCreditEntriesRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.ICourseProgressService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IMissionProgressService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.ISkillCreditsService;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules.Scored;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MissionProgressService implements IMissionProgressService {

    // Riga unica: il DB impone academy_settings_id = 1
    private static final short ACADEMY_SETTINGS_ID = 1;

    private final ExerciseAssessmentsRepository assessmentsRepository;
    private final MissionAttemptsRepository missionAttemptsRepository;
    private final MissionCheckpointsRepository checkpointsRepository;
    private final MissionExerciseSkillsRepository exerciseSkillsRepository;
    private final MissionSkillContributionsRepository contributionsRepository;
    private final MissionRecordsRepository recordsRepository;
    private final SkillCreditEntriesRepository creditEntriesRepository;
    private final AcademySettingsRepository academySettingsRepository;
    private final StudentsRepository studentsRepository;
    private final ISkillCreditsService skillCreditsService;
    private final IStudentXpService studentXpService;
    private final ICourseProgressService courseProgressService;
    private final EntityManager entityManager;

    public MissionProgressService(ExerciseAssessmentsRepository assessmentsRepository,
                                  MissionAttemptsRepository missionAttemptsRepository,
                                  MissionCheckpointsRepository checkpointsRepository,
                                  MissionExerciseSkillsRepository exerciseSkillsRepository,
                                  MissionSkillContributionsRepository contributionsRepository,
                                  MissionRecordsRepository recordsRepository,
                                  SkillCreditEntriesRepository creditEntriesRepository,
                                  AcademySettingsRepository academySettingsRepository,
                                  StudentsRepository studentsRepository, ISkillCreditsService skillCreditsService,
                                  IStudentXpService studentXpService, ICourseProgressService courseProgressService,
                                  EntityManager entityManager) {
        this.assessmentsRepository = assessmentsRepository;
        this.missionAttemptsRepository = missionAttemptsRepository;
        this.checkpointsRepository = checkpointsRepository;
        this.exerciseSkillsRepository = exerciseSkillsRepository;
        this.contributionsRepository = contributionsRepository;
        this.recordsRepository = recordsRepository;
        this.creditEntriesRepository = creditEntriesRepository;
        this.academySettingsRepository = academySettingsRepository;
        this.studentsRepository = studentsRepository;
        this.skillCreditsService = skillCreditsService;
        this.studentXpService = studentXpService;
        this.courseProgressService = courseProgressService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public SkillPointsDTO onExerciseAssessed(long studentId, long exerciseAttemptId) {
        ExerciseAssessment assessment = assessmentsRepository.findById(exerciseAttemptId)
                .orElseThrow(() -> new NotFoundException("Il tentativo " + exerciseAttemptId + " non ha ancora un esito"));
        ExerciseAttempt attempt = assessment.getExerciseAttempt();
        if (attempt.getContext() != ExerciseContext.mission) {
            throw new IllegalStateException("Il tentativo " + exerciseAttemptId + " non è di una missione");
        }
        MissionExercise exercise = attempt.getMissionExercise();
        Mission mission = exercise.getMission();
        long subjectId = mission.getCourse().getSubject().getSubjectId();
        // Punti massimi e fattore: quelli del momento (i punti dal tentativo, §6.1; il fattore resta nel contributo)
        BigDecimal maxPoints = AttemptJson.maxPoints(attempt.getConfigurationSnapshot());
        if (maxPoints == null) {
            maxPoints = exercise.getMaxPoints();
        }
        BigDecimal factor = settings().getMissionSkillFactor();

        BigDecimal earned = PlayRules.ZERO;
        List<MissionExerciseSkill> skills = exerciseSkillsRepository.findWithSkill(exercise.getMissionExerciseId());
        for (MissionExerciseSkill share : skills) {
            BigDecimal contribution = PlayRules.missionSkillContribution(maxPoints, share.getWeight(),
                    assessment.getPercentage(), factor);
            // Solo la differenza rispetto al migliore risultato in questo esercizio (Svolgimento §5)
            earned = earned.add(skillCreditsService.creditImprovement(studentId, subjectId, share.getSkill().getSkillId(),
                    SourceKind.mission, exercise.getMissionExerciseId(), contribution, "mission:" + exerciseAttemptId));
        }
        if (earned.signum() == 0) {
            return new SkillPointsDTO(PlayRules.ZERO, List.of());
        }
        entityManager.flush();
        for (MissionExerciseSkill share : skills) {
            updateContribution(studentId, mission, share, attempt.getMissionAttempt(), factor);
        }
        return new SkillPointsDTO(earned, studentXpService.onSkillPointsCredited(studentId));
    }

    @Override
    @Transactional
    public MissionPassedDTO onMissionPassed(long studentId, long missionAttemptId) {
        MissionAttempt missionAttempt = missionAttemptsRepository.findById(missionAttemptId)
                .orElseThrow(() -> new NotFoundException("Tentativo di missione " + missionAttemptId + " non trovato"));
        Mission mission = missionAttempt.getMissionRun().getMission();
        // Risultato: gli esercizi superati nel percorso, pesati sui punti (§9.3). Nessun esercizio: 100
        List<Scored> scored = new ArrayList<>();
        for (MissionCheckpoint checkpoint : checkpointsRepository.findWithAssessment(missionAttempt.getMissionRun().getMissionRunId())) {
            ExerciseAssessment assessment = checkpoint.getExerciseAssessment();
            BigDecimal maxPoints = AttemptJson.maxPoints(assessment.getExerciseAttempt().getConfigurationSnapshot());
            scored.add(new Scored(maxPoints != null ? maxPoints : BigDecimal.ONE, assessment.getPercentage()));
        }
        BigDecimal percentage = PlayRules.missionPercentage(scored);
        boolean newRecord = updateRecord(studentId, mission, missionAttempt, percentage);

        BigDecimal xpBefore = studentXpService.getTotalXp(studentId);
        List<Integer> stages = new ArrayList<>(studentXpService.onMissionPassed(studentId, mission.getMissionId(), missionAttemptId));
        BigDecimal xpEarned = studentXpService.getTotalXp(studentId).subtract(xpBefore);
        CourseResultDTO course = courseProgressService.onMissionPassed(studentId, mission.getMissionId());
        stages.addAll(course.newStages());
        return new MissionPassedDTO(percentage, newRecord, xpEarned, stages, course);
    }

    // Il migliore risultato della missione; uno uguale o peggiore non lo tocca
    private boolean updateRecord(long studentId, Mission mission, MissionAttempt missionAttempt, BigDecimal percentage) {
        MissionRecord.MissionRecordId id = new MissionRecord.MissionRecordId(studentId, mission.getMissionId());
        MissionRecord record = recordsRepository.findById(id).orElse(null);
        if (record == null) {
            // persist e non save: con la chiave composta già valorizzata save farebbe un merge
            entityManager.persist(MissionRecord.builder().id(id)
                    .student(studentsRepository.getReferenceById(studentId)).mission(mission)
                    .missionAttempt(missionAttempt).percentage(percentage).build());
            return true;
        }
        if (percentage.compareTo(record.getPercentage()) > 0) {
            record.setMissionAttempt(missionAttempt);
            record.setPercentage(percentage);
            return true;
        }
        return false;
    }

    // Punti della skill nella missione = somma dei migliori contributi dei suoi esercizi
    private void updateContribution(long studentId, Mission mission, MissionExerciseSkill share,
                                    MissionAttempt missionAttempt, BigDecimal factor) {
        BigDecimal points = creditEntriesRepository.sumBestMissionContributions(studentId, mission.getMissionId(),
                share.getSkill().getSkillId());
        MissionSkillContribution.MissionSkillContributionId id = new MissionSkillContribution.MissionSkillContributionId(
                studentId, mission.getMissionId(), share.getSkill().getSkillId());
        MissionSkillContribution contribution = contributionsRepository.findById(id).orElse(null);
        if (contribution == null) {
            entityManager.persist(MissionSkillContribution.builder().id(id)
                    .student(studentsRepository.getReferenceById(studentId)).mission(mission).skill(share.getSkill())
                    .missionAttempt(missionAttempt).points(points).factorSnapshot(factor).build());
        } else if (points.compareTo(contribution.getPoints()) > 0) {
            contribution.setPoints(points);
            contribution.setFactorSnapshot(factor);
            contribution.setMissionAttempt(missionAttempt);
        }
    }

    private AcademySetting settings() {
        return academySettingsRepository.findById(ACADEMY_SETTINGS_ID)
                .orElseThrow(() -> new NotFoundException("Impostazioni dell'Academy non trovate"));
    }
}
