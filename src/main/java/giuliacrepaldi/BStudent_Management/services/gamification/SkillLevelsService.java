package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.entities.skills.SkillLevel;
import giuliacrepaldi.BStudent_Management.entities.skills.StudentStartingLevel;
import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeLockDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelsDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.SkillCreditEntriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillLevelsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.StudentStartingLevelsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SubjectSkillsRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.utilities.GameRules;
import giuliacrepaldi.BStudent_Management.utilities.GameRules.LevelState;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SkillLevelsService implements ISkillLevelsService {

    // Una skill senza soglie ha solo il livello 1
    private static final List<BigDecimal> ONLY_FIRST_LEVEL = List.of(BigDecimal.ZERO);

    private final SkillLevelsRepository skillLevelsRepository;
    private final SkillCreditEntriesRepository skillCreditEntriesRepository;
    private final SubjectSkillsRepository subjectSkillsRepository;
    private final StudentStartingLevelsRepository startingLevelsRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;
    private final SkillsRepository skillsRepository;
    private final SubjectsRepository subjectsRepository;
    private final EntityManager entityManager;

    public SkillLevelsService(SkillLevelsRepository skillLevelsRepository,
                              SkillCreditEntriesRepository skillCreditEntriesRepository,
                              SubjectSkillsRepository subjectSkillsRepository,
                              StudentStartingLevelsRepository startingLevelsRepository,
                              PracticeExercisesRepository practiceExercisesRepository,
                              SkillsRepository skillsRepository, SubjectsRepository subjectsRepository,
                              EntityManager entityManager) {
        this.skillLevelsRepository = skillLevelsRepository;
        this.skillCreditEntriesRepository = skillCreditEntriesRepository;
        this.subjectSkillsRepository = subjectSkillsRepository;
        this.startingLevelsRepository = startingLevelsRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
        this.skillsRepository = skillsRepository;
        this.subjectsRepository = subjectsRepository;
        this.entityManager = entityManager;
    }

    // Materia + skill: punti e livelli si contano per materia (blueprint §10.4-ter)
    private record Key(long subjectId, long skillId) {
    }

    // Tutto ciò che serve per calcolare i livelli di uno studente, letto una volta sola
    private record Facts(Map<Key, BigDecimal> points, Map<Key, Integer> startingLevels,
                         Map<Long, List<BigDecimal>> thresholds) {

        BigDecimal pointsOf(Key key) {
            return points.getOrDefault(key, BigDecimal.ZERO);
        }

        List<BigDecimal> thresholdsOf(long skillId) {
            return thresholds.getOrDefault(skillId, ONLY_FIRST_LEVEL);
        }

        LevelState stateOf(Key key) {
            List<BigDecimal> curve = thresholdsOf(key.skillId());
            return GameRules.levelState(GameRules.levelPoints(pointsOf(key), startingLevels.get(key), curve), curve);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillLevelStateDTO> getLevels(long studentId, long subjectId) {
        if (!subjectsRepository.existsById(subjectId)) {
            throw new NotFoundException("Materia " + subjectId + " non trovata");
        }
        List<SubjectSkill> subjectSkills = subjectSkillsRepository.findBySubjectOrdered(subjectId);
        Facts facts = factsOf(studentId, subjectSkills.stream().map(s -> s.getId().getSkillId()).toList());
        List<SkillLevelStateDTO> result = new ArrayList<>();
        for (SubjectSkill subjectSkill : subjectSkills) {
            Skill skill = subjectSkill.getSkill();
            Key key = new Key(subjectId, skill.getSkillId());
            LevelState state = facts.stateOf(key);
            BigDecimal levelPoints = GameRules.levelPoints(facts.pointsOf(key), facts.startingLevels().get(key),
                    facts.thresholdsOf(skill.getSkillId()));
            result.add(new SkillLevelStateDTO(skill.getSkillId(), skill.getCodeSkill(), skill.getSkillName(),
                    skill.getIcon(), skill.getColor(), facts.pointsOf(key), state.level(), state.maxLevel(),
                    state.progressPercent(),
                    state.maxLevel() ? null : state.nextMinPoints().subtract(levelPoints)));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public int getLevel(long studentId, long subjectId, long skillId) {
        return factsOf(studentId, List.of(skillId)).stateOf(new Key(subjectId, skillId)).level();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageLevel(long studentId, long subjectId) {
        List<Long> skillIds = subjectSkillsRepository.findSkillIdsWithPublishedExercises(subjectId);
        return averageLevel(factsOf(studentId, skillIds), subjectId, skillIds);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBestAverageLevel(long studentId) {
        // Solo le materie in cui lo studente ha fatto qualcosa: una materia mai iniziata non conta (§4.2)
        Facts facts = factsOf(studentId, null);
        Set<Long> subjectIds = new HashSet<>();
        facts.points().keySet().forEach(key -> subjectIds.add(key.subjectId()));
        facts.startingLevels().keySet().forEach(key -> subjectIds.add(key.subjectId()));
        BigDecimal best = GameRules.averageLevel(List.of());
        for (Long subjectId : subjectIds) {
            List<Long> skillIds = subjectSkillsRepository.findSkillIdsWithPublishedExercises(subjectId);
            BigDecimal average = averageLevel(facts, subjectId, skillIds);
            if (average.compareTo(best) > 0) {
                best = average;
            }
        }
        return best;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBestPoints(long studentId, long skillId) {
        BigDecimal best = BigDecimal.ZERO;
        for (Object[] row : skillCreditEntriesRepository.findTotalsByStudent(studentId)) {
            BigDecimal points = (BigDecimal) row[2];
            if (((Number) row[1]).longValue() == skillId && points.compareTo(best) > 0) {
                best = points;
            }
        }
        return best;
    }

    @Override
    @Transactional(readOnly = true)
    public PracticeLockDTO getLock(long studentId, long practiceExerciseId) {
        PracticeLockDTO lock = getLocks(studentId, List.of(practiceExerciseId)).get(practiceExerciseId);
        if (lock == null) {
            throw new NotFoundException("Voce della Palestra " + practiceExerciseId + " non trovata");
        }
        return lock;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, PracticeLockDTO> getLocks(long studentId, Collection<Long> practiceExerciseIds) {
        List<PracticeExercise> exercises = practiceExercisesRepository.findAllById(practiceExerciseIds);
        Facts facts = factsOf(studentId, exercises.stream().map(e -> e.getSkill().getSkillId()).distinct().toList());
        Map<Long, PracticeLockDTO> locks = new LinkedHashMap<>();
        for (PracticeExercise exercise : exercises) {
            // La materia della voce si ricava dall'argomento (§7.1)
            Key key = new Key(exercise.getTopic().getSubject().getSubjectId(), exercise.getSkill().getSkillId());
            boolean unlocked = GameRules.isUnlocked(exercise.getDifficulty(), facts.stateOf(key).level());
            int unlockLevel = GameRules.unlockLevel(exercise.getDifficulty());
            locks.put(exercise.getPracticeExerciseId(), new PracticeLockDTO(exercise.getPracticeExerciseId(),
                    exercise.getDifficulty(), !unlocked, unlocked ? null : unlockLevel,
                    unlocked ? null : "Si sblocca al livello " + unlockLevel + " di " + exercise.getSkill().getSkillName()));
        }
        return locks;
    }

    @Override
    @Transactional
    public void setStartingLevel(long studentId, long subjectId, long skillId, int level) {
        if (!subjectSkillsRepository.existsById_SubjectIdAndId_SkillId(subjectId, skillId)) {
            throw new BadRequestException("La skill " + skillId + " non è tra le skill della materia");
        }
        int maxLevel = getMaxLevel(skillId);
        if (level < 1 || level > maxLevel) {
            throw new BadRequestException("Il livello di partenza deve essere tra 1 e " + maxLevel);
        }
        StudentStartingLevel.StudentStartingLevelId id =
                new StudentStartingLevel.StudentStartingLevelId(studentId, subjectId, skillId);
        StudentStartingLevel existing = startingLevelsRepository.findById(id).orElse(null);
        if (existing == null) {
            // persist e non save: con la chiave composta già valorizzata save farebbe un merge
            entityManager.persist(StudentStartingLevel.builder()
                    .id(id).levelNumber(level).setAt(OffsetDateTime.now()).build());
        } else if (existing.getLevelNumber() < level) {
            // Può solo salire: rifare il test non fa scendere (§2.6)
            existing.setLevelNumber(level);
            existing.setSetAt(OffsetDateTime.now());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillLevel> findLevels(long skillId) {
        findSkill(skillId);
        return skillLevelsRepository.findBySkill_SkillIdOrderByLevelNumber(skillId);
    }

    @Override
    @Transactional
    public List<SkillLevel> replaceLevels(long skillId, SkillLevelsDTO body) {
        List<BigDecimal> curve = body.minPoints();
        if (curve.get(0).signum() != 0) {
            throw new BadRequestException("Il livello 1 parte sempre da 0 punti");
        }
        for (int i = 1; i < curve.size(); i++) {
            if (curve.get(i).compareTo(curve.get(i - 1)) <= 0) {
                throw new BadRequestException("Ogni livello deve chiedere più punti del precedente: controlla il livello " + (i + 1));
            }
        }
        List<Skill> targets = body.applyToAllSkills() ? skillsRepository.findAll() : List.of(findSkill(skillId));
        // Prima tutti i controlli, poi le modifiche: o cambia tutto o non cambia nulla
        for (Skill skill : targets) {
            assertNobodyLosesLevels(skill, curve);
        }
        for (Skill skill : targets) {
            // Cancella e riscrive: aggiornare le righe una per una urterebbe il vincolo unico (skill, punti)
            skillLevelsRepository.deleteBySkillId(skill.getSkillId());
            entityManager.flush();
            saveCurve(skill, curve);
        }
        entityManager.flush();
        entityManager.clear();
        return skillLevelsRepository.findBySkill_SkillIdOrderByLevelNumber(skillId);
    }

    @Override
    @Transactional
    public void createDefaultLevels(long skillId) {
        Skill skill = findSkill(skillId);
        if (skillLevelsRepository.findBySkill_SkillIdOrderByLevelNumber(skillId).isEmpty()) {
            saveCurve(skill, GameRules.DEFAULT_LEVEL_CURVE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getMaxLevel(long skillId) {
        return Math.max(1, skillLevelsRepository.findBySkill_SkillIdOrderByLevelNumber(skillId).size());
    }

    private void saveCurve(Skill skill, List<BigDecimal> curve) {
        for (int i = 0; i < curve.size(); i++) {
            skillLevelsRepository.save(SkillLevel.builder()
                    .skill(skill).levelNumber(i + 1).minPoints(curve.get(i)).build());
        }
    }

    // Il livello non scende mai (§2.3): niente livelli tolti e niente soglie alzate
    private void assertNobodyLosesLevels(Skill skill, List<BigDecimal> curve) {
        List<SkillLevel> current = skillLevelsRepository.findBySkill_SkillIdOrderByLevelNumber(skill.getSkillId());
        if (curve.size() < current.size()) {
            throw new ConflictException(skill.getSkillName() + " ha " + current.size()
                    + " livelli: non si possono togliere, perché chi li ha raggiunti scenderebbe");
        }
        for (int i = 0; i < current.size(); i++) {
            if (curve.get(i).compareTo(current.get(i).getMinPoints()) > 0) {
                throw new ConflictException("Il livello " + (i + 1) + " di " + skill.getSkillName() + " oggi chiede "
                        + current.get(i).getMinPoints().stripTrailingZeros().toPlainString()
                        + " punti: puoi aggiungere livelli o abbassare le soglie, non alzarle");
            }
        }
    }

    // skillIds = skill di cui servono le soglie; null = tutte quelle in cui lo studente ha punti o un livello
    private Facts factsOf(long studentId, Collection<Long> skillIds) {
        Map<Key, BigDecimal> points = new HashMap<>();
        for (Object[] row : skillCreditEntriesRepository.findTotalsByStudent(studentId)) {
            points.put(new Key(((Number) row[0]).longValue(), ((Number) row[1]).longValue()), (BigDecimal) row[2]);
        }
        Map<Key, Integer> startingLevels = new HashMap<>();
        for (StudentStartingLevel starting : startingLevelsRepository.findById_StudentId(studentId)) {
            startingLevels.put(new Key(starting.getId().getSubjectId(), starting.getId().getSkillId()),
                    starting.getLevelNumber());
        }
        Set<Long> wanted = new HashSet<>();
        if (skillIds != null) {
            wanted.addAll(skillIds);
        }
        points.keySet().forEach(key -> wanted.add(key.skillId()));
        startingLevels.keySet().forEach(key -> wanted.add(key.skillId()));
        Map<Long, List<BigDecimal>> thresholds = new HashMap<>();
        if (!wanted.isEmpty()) {
            for (SkillLevel level : skillLevelsRepository.findBySkillIds(wanted)) {
                thresholds.computeIfAbsent(level.getSkill().getSkillId(), id -> new ArrayList<>())
                        .add(level.getMinPoints());
            }
        }
        return new Facts(points, startingLevels, thresholds);
    }

    private static BigDecimal averageLevel(Facts facts, long subjectId, List<Long> skillIds) {
        return GameRules.averageLevel(skillIds.stream()
                .map(skillId -> facts.stateOf(new Key(subjectId, skillId)).level())
                .toList());
    }

    private Skill findSkill(long skillId) {
        return skillsRepository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill " + skillId + " non trovata"));
    }
}
