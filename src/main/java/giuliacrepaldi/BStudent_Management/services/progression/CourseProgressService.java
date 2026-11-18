package giuliacrepaldi.BStudent_Management.services.progression;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.progression.CourseResult;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.progression.CourseResultDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.CourseResultsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.MissionRecordsRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.ICourseProgressService;
import giuliacrepaldi.BStudent_Management.utilities.PlayRules;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CourseProgressService implements ICourseProgressService {

    private final MissionsRepository missionsRepository;
    private final MissionAttemptsRepository missionAttemptsRepository;
    private final MissionRecordsRepository missionRecordsRepository;
    private final CourseResultsRepository courseResultsRepository;
    private final StudentsRepository studentsRepository;
    private final IStudentXpService studentXpService;
    private final EntityManager entityManager;

    public CourseProgressService(MissionsRepository missionsRepository, MissionAttemptsRepository missionAttemptsRepository,
                                 MissionRecordsRepository missionRecordsRepository,
                                 CourseResultsRepository courseResultsRepository, StudentsRepository studentsRepository,
                                 IStudentXpService studentXpService, EntityManager entityManager) {
        this.missionsRepository = missionsRepository;
        this.missionAttemptsRepository = missionAttemptsRepository;
        this.missionRecordsRepository = missionRecordsRepository;
        this.courseResultsRepository = courseResultsRepository;
        this.studentsRepository = studentsRepository;
        this.studentXpService = studentXpService;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public CourseResultDTO onMissionPassed(long studentId, long missionId) {
        Mission passedMission = missionsRepository.findById(missionId)
                .orElseThrow(() -> new NotFoundException("Missione " + missionId + " non trovata"));
        Course course = passedMission.getCourse();
        // Le letture native qui sotto devono vedere il tentativo appena superato
        entityManager.flush();
        List<Long> visible = missionsRepository
                .findByCourse_CourseIdAndStatusOrderByMissionSortOrder(course.getCourseId(), ContentStatus.published)
                .stream().map(Mission::getMissionId).toList();
        Set<Long> passed = new HashSet<>(missionAttemptsRepository.findPassedMissionIds(studentId, course.getCourseId()));
        List<Long> passedVisible = visible.stream().filter(passed::contains).toList();
        // Il DB vuole almeno una missione nel totale: un corso senza missioni visibili non ha risultato
        if (visible.isEmpty()) {
            return new CourseResultDTO(course.getCourseId(), course.getCourseTitle(), 0, 0, null, false, List.of());
        }
        BigDecimal average = passedVisible.isEmpty() ? null
                : PlayRules.courseAverage(missionRecordsRepository.findPercentages(studentId, passedVisible));
        boolean allPassed = passedVisible.size() == visible.size();

        CourseResult.CourseResultId id = new CourseResult.CourseResultId(studentId, course.getCourseId());
        CourseResult existing = courseResultsRepository.findById(id).orElse(null);
        CourseResult result = existing != null ? existing : CourseResult.builder().id(id)
                .student(studentsRepository.getReferenceById(studentId)).course(course).build();
        boolean completedNow = allPassed && result.getCompletedAt() == null;
        result.setPassedMissions(passedVisible.size());
        result.setTotalMissions(visible.size());
        result.setPercentage(average);
        // Vedi il TODO dell'interfaccia: completato vuol dire tutte le missioni visibili superate
        result.setCompletedAt(allPassed ? (completedNow ? OffsetDateTime.now() : result.getCompletedAt()) : null);
        if (existing == null) {
            // persist e non save: con la chiave composta già valorizzata save farebbe un merge
            entityManager.persist(result);
        }

        // Evoluzione I e base delle vite a 7: IStudentXpService la assegna una volta sola, al primo corso
        List<Integer> stages = completedNow ? studentXpService.onCourseCompleted(studentId, course.getCourseId()) : List.of();
        return new CourseResultDTO(course.getCourseId(), course.getCourseTitle(), passedVisible.size(), visible.size(),
                average, completedNow, stages);
    }
}
