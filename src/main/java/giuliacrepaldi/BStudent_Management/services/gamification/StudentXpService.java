package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.progression.StudentXpEntry;
import giuliacrepaldi.BStudent_Management.entities.skills.EvolutionStageRule;
import giuliacrepaldi.BStudent_Management.entities.skills.StudentEvolutionStage;
import giuliacrepaldi.BStudent_Management.enums.skills.EvolutionRuleKind;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionStageRuleDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.StudentXpEntriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.EvolutionStageRulesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentEvolutionStagesRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import giuliacrepaldi.BStudent_Management.utilities.GameRules;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentXpService implements IStudentXpService {

    // Premio dell'Evoluzione I: la base delle vite passa da 5 a 7 (blueprint §10.4).
    // TODO: portarlo nelle impostazioni di gioco se dovrà cambiare
    private static final int FIRST_EVOLUTION_BASE_LIVES = 7;

    private final StudentXpEntriesRepository xpEntriesRepository;
    private final StudentEvolutionStagesRepository stagesRepository;
    private final EvolutionStageRulesRepository rulesRepository;
    private final MissionExercisesRepository missionExercisesRepository;
    private final MissionsRepository missionsRepository;
    private final ISkillLevelsService skillLevelsService;
    private final ILivesService livesService;
    private final EntityManager entityManager;

    public StudentXpService(StudentXpEntriesRepository xpEntriesRepository,
                            StudentEvolutionStagesRepository stagesRepository,
                            EvolutionStageRulesRepository rulesRepository,
                            MissionExercisesRepository missionExercisesRepository,
                            MissionsRepository missionsRepository, ISkillLevelsService skillLevelsService,
                            ILivesService livesService, EntityManager entityManager) {
        this.xpEntriesRepository = xpEntriesRepository;
        this.stagesRepository = stagesRepository;
        this.rulesRepository = rulesRepository;
        this.missionExercisesRepository = missionExercisesRepository;
        this.missionsRepository = missionsRepository;
        this.skillLevelsService = skillLevelsService;
        this.livesService = livesService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalXp(long studentId) {
        return xpEntriesRepository.sumXp(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getCurrentStage(long studentId) {
        return stagesRepository.findCurrentStage(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public EvolutionProgressDTO getEvolutionProgress(long studentId) {
        int current = stagesRepository.findCurrentStage(studentId);
        BigDecimal xp = xpEntriesRepository.sumXp(studentId);
        BigDecimal average = skillLevelsService.getBestAverageLevel(studentId);
        EvolutionStageRule next = rulesRepository.findAllByOrderByStageNumber().stream()
                .filter(rule -> rule.getStageNumber() > current)
                .findFirst().orElse(null);
        if (next == null) {
            return new EvolutionProgressDTO(current, xp, average, null, false, null, null, null, false);
        }
        if (next.getRuleKind() != EvolutionRuleKind.thresholds) {
            return new EvolutionProgressDTO(current, xp, average, next.getStageNumber(), true, null, null, null, false);
        }
        return new EvolutionProgressDTO(current, xp, average, next.getStageNumber(), false, next.getMinXp(),
                next.getMinXp().subtract(xp).max(BigDecimal.ZERO), next.getMinAverageLevel(),
                average.compareTo(next.getMinAverageLevel()) >= 0);
    }

    @Override
    @Transactional
    public List<Integer> onMissionPassed(long studentId, long missionId, long missionAttemptId) {
        // Solo al primo superamento: ripetizioni e doppi invii non riaccreditano (§10.3).
        // Il vincolo unico (studente, missione) resta l'ultima difesa in caso di richieste contemporanee
        if (!xpEntriesRepository.existsByStudentIdAndMissionId(studentId, missionId)) {
            if (!missionsRepository.existsById(missionId)) {
                throw new NotFoundException("Missione " + missionId + " non trovata");
            }
            // Gli XP si fissano adesso: cambiare poi i punti degli esercizi non li modifica (§1)
            BigDecimal xp = missionExercisesRepository.sumVisibleMaxPoints(missionId);
            if (xp.signum() > 0) {
                xpEntriesRepository.save(StudentXpEntry.builder()
                        .studentId(studentId)
                        .missionId(missionId)
                        .missionAttemptId(missionAttemptId)
                        .xp(xp)
                        .awardedAt(OffsetDateTime.now())
                        .build());
            }
        }
        return evaluateEvolutions(studentId);
    }

    @Override
    @Transactional
    public List<Integer> onSkillPointsCredited(long studentId) {
        return evaluateEvolutions(studentId);
    }

    @Override
    @Transactional
    public List<Integer> onCourseCompleted(long studentId, long courseId) {
        List<Integer> reached = new ArrayList<>();
        int current = stagesRepository.findCurrentStage(studentId);
        EvolutionStageRule firstCourse = rulesRepository.findAllByOrderByStageNumber().stream()
                .filter(rule -> rule.getRuleKind() == EvolutionRuleKind.first_course)
                .findFirst().orElse(null);
        // Una sola volta: i corsi completati dopo il primo non assegnano di nuovo il premio (§10.4)
        if (firstCourse != null && firstCourse.getStageNumber() == current + 1) {
            reach(studentId, firstCourse.getStageNumber());
            livesService.raiseBaseLives(studentId, FIRST_EVOLUTION_BASE_LIVES);
            reached.add(firstCourse.getStageNumber());
        }
        reached.addAll(evaluateEvolutions(studentId));
        return reached;
    }

    @Override
    @Transactional
    public List<Integer> evaluateEvolutions(long studentId) {
        List<Integer> reached = new ArrayList<>();
        int current = stagesRepository.findCurrentStage(studentId);
        BigDecimal xp = null;
        BigDecimal average = null;
        for (EvolutionStageRule rule : rulesRepository.findAllByOrderByStageNumber()) {
            if (rule.getStageNumber() <= current) {
                continue;
            }
            // In ordine: uno stadio richiede il precedente. L'Evoluzione I arriva solo dal corso completato
            if (rule.getRuleKind() != EvolutionRuleKind.thresholds) {
                break;
            }
            if (xp == null) {
                xp = xpEntriesRepository.sumXp(studentId);
                average = skillLevelsService.getBestAverageLevel(studentId);
            }
            if (!GameRules.meetsStage(xp, average, rule.getMinXp(), rule.getMinAverageLevel())) {
                break;
            }
            reach(studentId, rule.getStageNumber());
            reached.add(rule.getStageNumber());
            current = rule.getStageNumber();
        }
        return reached;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvolutionStageRule> findRules() {
        return rulesRepository.findAllByOrderByStageNumber();
    }

    @Override
    @Transactional
    public EvolutionStageRule updateRule(int stageNumber, EvolutionStageRuleDTO body) {
        EvolutionStageRule rule = rulesRepository.findById(stageNumber)
                .orElseThrow(() -> new NotFoundException("Stadio " + stageNumber + " non trovato"));
        if (rule.getRuleKind() != EvolutionRuleKind.thresholds) {
            throw new BadRequestException("Questo stadio non ha soglie da modificare: "
                    + (rule.getRuleKind() == EvolutionRuleKind.start
                    ? "è lo stadio di partenza" : "si raggiunge completando il primo corso"));
        }
        // Chi ha già raggiunto lo stadio lo conserva comunque (§4.5)
        rule.setMinXp(body.minXp());
        rule.setMinAverageLevel(body.minAverageLevel());
        return rule;
    }

    private void reach(long studentId, int stageNumber) {
        // persist e non save: con la chiave composta già valorizzata save farebbe un merge
        entityManager.persist(StudentEvolutionStage.builder()
                .id(new StudentEvolutionStage.StudentEvolutionStageId(studentId, stageNumber))
                .reachedAt(OffsetDateTime.now())
                .build());
        entityManager.flush();
    }
}
