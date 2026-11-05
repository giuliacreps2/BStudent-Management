package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.catalog.MissionDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExerciseSkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExercisesRepository;
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

    private final MissionsRepository missionsRepository;
    private final LessonsRepository lessonsRepository;
    private final MissionExercisesRepository missionExercisesRepository;
    private final MissionExerciseSkillsRepository missionExerciseSkillsRepository;
    private final QuestionVersionsRepository questionVersionsRepository;
    private final ICoursesService coursesService;
    private final EntityManager entityManager;

    public MissionsService(MissionsRepository missionsRepository, LessonsRepository lessonsRepository,
                           MissionExercisesRepository missionExercisesRepository,
                           MissionExerciseSkillsRepository missionExerciseSkillsRepository,
                           QuestionVersionsRepository questionVersionsRepository,
                           ICoursesService coursesService, EntityManager entityManager) {
        this.missionsRepository = missionsRepository;
        this.lessonsRepository = lessonsRepository;
        this.missionExercisesRepository = missionExercisesRepository;
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

    @Override
    @Transactional
    public Mission publishMission(long missionId) {
        Mission mission = findById(missionId);
        if (mission.getStatus() == ContentStatus.published) {
            throw new ConflictException("La missione è già pubblicata");
        }
        List<String> problems = readinessProblems(mission);
        if (!problems.isEmpty()) {
            throw new ConflictException("La missione non è pronta: " + String.join("; ", problems));
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
        if (lessonsRepository.existsByMission_MissionId(missionId)) {
            throw new ConflictException("La missione ha delle lezioni: eliminale prima");
        }
        // Il buco nell'ordine (es. 1, 3) non viola il vincolo: si sistema con il riordino
        missionsRepository.delete(mission);
    }

    // Controllo "missione pronta": lo studente vede solo lezioni pubblicate, quindi conta solo quelle
    private List<String> readinessProblems(Mission mission) {
        Long missionId = mission.getMissionId();
        List<String> problems = new ArrayList<>();

        if (!lessonsRepository.existsByMission_MissionIdAndStatus(missionId, ContentStatus.published)) {
            problems.add("nessuna lezione pubblicata");
            return problems;
        }

        List<String> withoutExercise = lessonsRepository.findTitlesWithoutExercise(missionId, ContentStatus.published);
        if (!withoutExercise.isEmpty()) {
            problems.add("lezioni senza esercizio: " + String.join(", ", withoutExercise));
        }

        boolean subjectHasDefaultSkill = mission.getCourse().getSubject().getDefaultSkill() != null;
        for (MissionExercise exercise : missionExercisesRepository.findByMissionAndLessonStatus(missionId, ContentStatus.published)) {
            String lessonTitle = exercise.getLesson().getLessonTitle();
            Long entryId = exercise.getQuestionBankEntry().getQuestionBankEntryId();

            if (exercise.getQuestionBankEntry().getStatus() != ContentStatus.published) {
                problems.add("esercizio non pubblicato nella lezione " + lessonTitle);
            } else if (!questionVersionsRepository.existsPublishedVersion(entryId)) {
                problems.add("esercizio senza versione pubblicata nella lezione " + lessonTitle);
            }

            // Pesi espliciti a somma 100, oppure nessun peso e fallback sulla skill predefinita della materia
            Long exerciseId = exercise.getMissionExerciseId();
            if (missionExerciseSkillsRepository.countById_MissionExerciseId(exerciseId) == 0) {
                if (!subjectHasDefaultSkill) {
                    problems.add("pesi skill mancanti nella lezione " + lessonTitle
                            + " e materia senza skill predefinita");
                }
            } else if (missionExerciseSkillsRepository.sumWeights(exerciseId).compareTo(FULL_WEIGHT) != 0) {
                problems.add("i pesi skill della lezione " + lessonTitle + " non sommano 100");
            }
        }
        return problems;
    }

    private static void applyContent(Mission mission, MissionDTO body) {
        mission.setMissionTitle(body.title().strip());
        mission.setMissionDescription(body.shortDescription().strip());
        // null = eredita dal corso; ammessa anche premium in un corso free
        mission.setAccessTier(body.accessTier());
    }
}
