package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.catalog.MissionDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.MissionReadinessDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionStepsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExerciseSkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionVersionsRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ICoursesService;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.IMissionsService;
import giuliacrepaldi.BStudent_Management.utilities.Reorders;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MissionsService implements IMissionsService {

    private static final BigDecimal FULL_WEIGHT = BigDecimal.valueOf(100);
    private static final String SORT_ORDER_CONSTRAINT = "missions_course_id_sort_order_idx";
    private static final String STEP_ORDER_CONSTRAINT = "mission_steps_mission_id_sort_order_idx";

    private final MissionsRepository missionsRepository;
    private final MissionStepsRepository missionStepsRepository;
    private final MissionExerciseSkillsRepository missionExerciseSkillsRepository;
    private final QuestionVersionsRepository questionVersionsRepository;
    private final ICoursesService coursesService;
    private final EntityManager entityManager;

    public MissionsService(MissionsRepository missionsRepository, MissionStepsRepository missionStepsRepository,
                           MissionExerciseSkillsRepository missionExerciseSkillsRepository,
                           QuestionVersionsRepository questionVersionsRepository,
                           ICoursesService coursesService, EntityManager entityManager) {
        this.missionsRepository = missionsRepository;
        this.missionStepsRepository = missionStepsRepository;
        this.missionExerciseSkillsRepository = missionExerciseSkillsRepository;
        this.questionVersionsRepository = questionVersionsRepository;
        this.coursesService = coursesService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Mission saveNewMission(long courseId, MissionDTO body) {
        Course course = coursesService.findById(courseId);
        // In coda: due creazioni contemporanee sullo stesso corso violano l'unicità -> 409
        Mission mission = Mission.builder()
                .course(course)
                .missionSortOrder(missionsRepository.findMaxSortOrder(courseId) + 1)
                .status(ContentStatus.draft)
                .build();
        applyContent(mission, body);
        return missionsRepository.save(mission);
    }

    @Override
    @Transactional(readOnly = true)
    public Mission findById(long missionId) {
        return missionsRepository.findById(missionId)
                .orElseThrow(() -> new NotFoundException(missionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mission> findByCourse(long courseId) {
        // 404 se il corso non esiste, invece di una lista vuota
        coursesService.findById(courseId);
        return missionsRepository.findByCourse_CourseIdOrderByMissionSortOrder(courseId);
    }

    @Override
    @Transactional
    public Mission findByIdAndUpdate(long missionId, MissionDTO body) {
        Mission mission = findById(missionId);
        // Ammessa in ogni stato: i contenuti pubblicati mostrano gli aggiornamenti (blueprint §5.3)
        applyContent(mission, body);
        return mission;
    }

    @Override
    @Transactional
    public List<Mission> reorderMissions(long courseId, List<Long> missionIds) {
        Map<Long, Mission> byId = findByCourse(courseId).stream()
                .collect(Collectors.toMap(Mission::getMissionId, Function.identity()));
        Reorders.validateIds(missionIds, byId.keySet(), "le missioni del corso");
        Reorders.deferUniqueOrder(entityManager, SORT_ORDER_CONSTRAINT);

        List<Mission> reordered = new ArrayList<>(missionIds.size());
        for (int i = 0; i < missionIds.size(); i++) {
            Mission mission = byId.get(missionIds.get(i));
            mission.setMissionSortOrder(i + 1);
            reordered.add(mission);
        }
        return reordered;
    }

    //SEQUENZA

    @Override
    @Transactional(readOnly = true)
    public List<MissionStep> findSteps(long missionId) {
        findById(missionId);
        return missionStepsRepository.findByMissionOrdered(missionId);
    }

    @Override
    @Transactional
    public List<MissionStep> reorderSteps(long missionId, List<Long> stepIds) {
        Mission mission = findById(missionId);
        Map<Long, MissionStep> byId = missionStepsRepository.findByMissionOrdered(missionId).stream()
                .collect(Collectors.toMap(MissionStep::getMissionStepId, Function.identity()));
        Reorders.validateIds(stepIds, byId.keySet(), "i passi della missione");
        Reorders.deferUniqueOrder(entityManager, STEP_ORDER_CONSTRAINT);

        List<MissionStep> reordered = new ArrayList<>(stepIds.size());
        for (int i = 0; i < stepIds.size(); i++) {
            MissionStep step = byId.get(stepIds.get(i));
            step.setStepSortOrder(i + 1);
            reordered.add(step);
        }
        assertValidSequence(mission, reordered);
        return reordered;
    }

    @Override
    @Transactional(readOnly = true)
    public MissionReadinessDTO checkReadiness(long missionId) {
        return readiness(findById(missionId));
    }

    @Override
    @Transactional
    public void assertSequenceStillValid(Mission mission) {
        assertValidSequence(mission, missionStepsRepository.findByMissionOrdered(mission.getMissionId()));
    }

    //STATO

    @Override
    @Transactional
    public Mission publishMission(long missionId) {
        Mission mission = findById(missionId);
        if (mission.getStatus() == ContentStatus.published) {
            throw new ConflictException("La missione è già pubblicata");
        }
        // Gli avvisi non bloccano: l'Admin li legge da checkReadiness prima di pubblicare
        List<String> problems = readiness(mission).problems();
        if (!problems.isEmpty()) {
            throw new ConflictException("La missione non è pronta: " + String.join("; ", problems));
        }
        // Gli esercizi in bozza escono con la missione (quelli ritirati nel banco restano nascosti)
        for (MissionStep step : missionStepsRepository.findByMissionOrdered(missionId)) {
            if (step.isExercise() && !isWithdrawnExercise(step)) {
                step.getMissionExercise().setStatus(ContentStatus.published);
            }
        }
        mission.setStatus(ContentStatus.published);
        // published_at è la prima pubblicazione: resta anche dopo un ritiro e un ripristino
        if (mission.getPublishedAt() == null) {
            mission.setPublishedAt(OffsetDateTime.now());
        }
        mission.setWithdrawnAt(null);
        return mission;
    }

    @Override
    @Transactional
    public Mission withdrawMission(long missionId) {
        Mission mission = findById(missionId);
        if (mission.getStatus() != ContentStatus.published) {
            throw new ConflictException("Si può ritirare solo una missione pubblicata");
        }
        // Un corso pubblicato deve avere almeno una missione pubblicata (stessa regola di publishCourse)
        Course course = mission.getCourse();
        if (course.getStatus() == ContentStatus.published
                && missionsRepository.countByCourse_CourseIdAndStatus(course.getCourseId(), ContentStatus.published) == 1) {
            throw new ConflictException("È l'ultima missione pubblicata del corso: ritira prima il corso");
        }
        // TODO (Blocco 5): comportamento dei tentativi già avviati su una missione ritirata, non ancora deciso
        mission.setStatus(ContentStatus.withdrawn);
        mission.setWithdrawnAt(OffsetDateTime.now());
        return mission;
    }

    @Override
    @Transactional
    public void deleteMission(long missionId) {
        Mission mission = findById(missionId);
        if (mission.getStatus() != ContentStatus.draft || mission.getPublishedAt() != null) {
            throw new ConflictException("Una missione già pubblicata non si elimina: va ritirata");
        }
        if (missionStepsRepository.existsByMission_MissionId(missionId)) {
            throw new ConflictException("La missione ha lezioni o esercizi: eliminali prima");
        }
        // Il buco nell'ordine (es. 1, 3) non viola il vincolo: si sistema con il riordino
        missionsRepository.delete(mission);
    }

    // Una missione pubblicata resta coerente per lo studente: ogni modifica alla sequenza
    // (riordino, esercizi, lezioni pubblicate o ritirate) deve lasciarla valida
    private static void assertValidSequence(Mission mission, List<MissionStep> steps) {
        if (mission.getStatus() != ContentStatus.published) {
            return;
        }
        List<String> problems = sequenceProblems(visibleSteps(steps, false));
        if (!problems.isEmpty()) {
            throw new ConflictException("La missione è pubblicata e la sequenza non sarebbe più valida: "
                    + String.join("; ", problems));
        }
    }

    // Controllo "missione pronta" sulla sequenza che vede (missione pubblicata) o vedrà lo studente.
    // Missione non pubblicata: gli esercizi in bozza contano, verranno pubblicati con lei
    private MissionReadinessDTO readiness(Mission mission) {
        boolean published = mission.getStatus() == ContentStatus.published;
        List<MissionStep> steps = missionStepsRepository.findByMissionOrdered(mission.getMissionId());
        List<MissionStep> visible = visibleSteps(steps, !published);
        List<String> problems = sequenceProblems(visible);
        List<String> warnings = new ArrayList<>();
        for (MissionStep step : steps) {
            if (isWithdrawnExercise(step)) {
                warnings.add("l'esercizio «" + exerciseTitle(step) + "» è ritirato: lo studente non lo vede");
            } else if (published && step.isExercise() && step.getMissionExercise().getStatus() == ContentStatus.draft) {
                warnings.add("l'esercizio «" + exerciseTitle(step) + "» è in bozza: lo studente non lo vede finché non lo pubblichi");
            }
        }
        if (!visible.isEmpty() && !visible.getLast().isExercise()) {
            warnings.add("la missione finisce con una lezione: di solito si chiude con un esercizio");
        }
        for (MissionStep step : visible) {
            if (step.isExercise()) {
                problems.addAll(exerciseProblems(step.getMissionExercise()));
            }
        }
        return new MissionReadinessDTO(problems, warnings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> exerciseProblems(MissionExercise exercise) {
        List<String> problems = new ArrayList<>();
        String title = "«" + exercise.getQuestionBankEntry().getInternalQuestionTitle() + "»";
        Long entryId = exercise.getQuestionBankEntry().getQuestionBankEntryId();

        if (exercise.getQuestionBankEntry().getStatus() != ContentStatus.published) {
            problems.add("l'esercizio " + title + " non è pubblicato");
        } else if (!questionVersionsRepository.existsPublishedVersion(entryId)) {
            problems.add("l'esercizio " + title + " non ha una versione pubblicata");
        }

        // Pesi espliciti a somma 100, oppure nessun peso e fallback sulla skill predefinita della materia.
        // TODO (gamification, blueprint §10.3): regole delle skill non ancora chiare; ricontrollare se
        //  devono bloccare la pubblicazione, il fallback sulla materia e come si calcolano i contributi
        Long exerciseId = exercise.getMissionExerciseId();
        if (missionExerciseSkillsRepository.countById_MissionExerciseId(exerciseId) == 0) {
            if (exercise.getMission().getCourse().getSubject().getDefaultSkill() == null) {
                problems.add("l'esercizio " + title + " non ha le skill indicate e la materia non ha una skill predefinita");
            }
        } else if (missionExerciseSkillsRepository.sumWeights(exerciseId).compareTo(FULL_WEIGHT) != 0) {
            problems.add("le skill dell'esercizio " + title + " non arrivano al 100%");
        }
        return problems;
    }

    // Lo studente vede le lezioni pubblicate e gli esercizi pubblicati nella missione e non ritirati nel banco
    private static List<MissionStep> visibleSteps(List<MissionStep> steps, boolean includeDraftExercises) {
        return steps.stream()
                .filter(step -> step.isExercise()
                        ? !isWithdrawnExercise(step)
                          && (includeDraftExercises || step.getMissionExercise().getStatus() == ContentStatus.published)
                        : step.getLesson().getStatus() == ContentStatus.published)
                .toList();
    }

    private static boolean isWithdrawnExercise(MissionStep step) {
        return step.isExercise() && step.getMissionExercise().getQuestionBankEntry().getStatus() == ContentStatus.withdrawn;
    }

    // Regole della sequenza (v0.8): inizia con una lezione, due esercizi non si susseguono,
    // almeno un esercizio. Due lezioni di seguito sono ammesse
    private static List<String> sequenceProblems(List<MissionStep> visible) {
        List<String> problems = new ArrayList<>();
        if (visible.stream().allMatch(MissionStep::isExercise)) {
            problems.add("nessuna lezione pubblicata");
        } else if (visible.getFirst().isExercise()) {
            problems.add("la missione deve iniziare con una lezione, ora inizia con l'esercizio «"
                    + exerciseTitle(visible.getFirst()) + "»");
        }
        for (int i = 1; i < visible.size(); i++) {
            if (visible.get(i - 1).isExercise() && visible.get(i).isExercise()) {
                problems.add("due esercizi di seguito: «" + exerciseTitle(visible.get(i - 1))
                        + "» e «" + exerciseTitle(visible.get(i)) + "»");
            }
        }
        if (visible.stream().noneMatch(MissionStep::isExercise)) {
            problems.add("nessun esercizio");
        }
        return problems;
    }

    private static String exerciseTitle(MissionStep step) {
        return step.getMissionExercise().getQuestionBankEntry().getInternalQuestionTitle();
    }

    private static void applyContent(Mission mission, MissionDTO body) {
        mission.setMissionTitle(body.title().strip());
        mission.setMissionDescription(body.shortDescription().strip());
        // null = eredita dal corso; ammessa anche premium in un corso free
        mission.setAccessTier(body.accessTier());
    }
}
