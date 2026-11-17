package giuliacrepaldi.BStudent_Management.services.student;

import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationTest;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationTestsRepository;
import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.student.CourseCardDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.CourseDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.CourseLandingDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.CoursesPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.LandingStepDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.SkillRefDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.SubjectCardDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.CoursesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentGameStateService;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentCatalogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class StudentCatalogService implements IStudentCatalogService {

    // Tappe mostrate nella landing pubblica (blueprint §5.8)
    private static final int LANDING_STEPS = 5;

    private final SubjectsRepository subjectsRepository;
    private final CoursesRepository coursesRepository;
    private final MissionsRepository missionsRepository;
    private final MissionAttemptsRepository missionAttemptsRepository;
    private final SkillsRepository skillsRepository;
    private final IEntitlementService entitlementService;
    private final OrientationTestsRepository testsRepository;
    private final IStudentGameStateService gameStateService;

    public StudentCatalogService(SubjectsRepository subjectsRepository, CoursesRepository coursesRepository,
                                 MissionsRepository missionsRepository,
                                 MissionAttemptsRepository missionAttemptsRepository, SkillsRepository skillsRepository,
                                 IEntitlementService entitlementService, IStudentGameStateService gameStateService,
                                 OrientationTestsRepository testsRepository) {
        this.testsRepository = testsRepository;
        this.subjectsRepository = subjectsRepository;
        this.coursesRepository = coursesRepository;
        this.missionsRepository = missionsRepository;
        this.missionAttemptsRepository = missionAttemptsRepository;
        this.skillsRepository = skillsRepository;
        this.entitlementService = entitlementService;
        this.gameStateService = gameStateService;
    }

    @Override
    @Transactional(readOnly = true)
    public CoursesPageDTO getCoursesPage(long studentId, String activeSubjectSlug) {
        List<Subject> subjects = subjectsRepository.findByStatusOrderBySubjectId(SubjectStatus.published);
        List<SubjectCardDTO> cards = new ArrayList<>();
        Subject firstOpen = null;
        Subject requested = null;
        for (Subject subject : subjects) {
            boolean open = hasCourses(subject);
            cards.add(SubjectCardDTO.from(subject, open));
            if (open && firstOpen == null) {
                firstOpen = subject;
            }
            if (open && subject.getSlug().equals(activeSubjectSlug)) {
                requested = subject;
            }
        }
        // La materia chiesta, se è aperta; altrimenti la prima aperta
        Subject active = requested != null ? requested : firstOpen;
        if (active == null) {
            return new CoursesPageDTO(cards, null, List.of(), null, false);
        }
        List<CourseCardDTO> courses = new ArrayList<>();
        for (Course course : visibleCourses(active)) {
            courses.add(toCard(course, studentId));
        }
        OrientationTest levelTest = testsRepository
                .findBySubject_SubjectIdAndKind(active.getSubjectId(), OrientationTestKind.course_level).orElse(null);
        CourseLevel recommended = null;
        if (levelTest != null) {
            List<Object[]> last = testsRepository.findLastResult(studentId, levelTest.getOrientationTestId());
            if (!last.isEmpty() && last.get(0)[0] != null) {
                recommended = CourseLevel.valueOf((String) last.get(0)[0]);
            }
        }
        // Se non ci sono corsi del livello consigliato il frontend li mostra tutti (proposta §8.2)
        return new CoursesPageDTO(cards, SubjectCardDTO.from(active, true), courses, recommended, levelTest != null);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailDTO getCourseDetail(long studentId, String courseSlug) {
        Course course = findVisibleCourse(courseSlug);
        List<SkillRefDTO> skills = skillsRepository.findAllById(coursesRepository.findVisibleSkillIds(course.getCourseId()))
                .stream().sorted(Comparator.comparing(skill -> skill.getSkillName()))
                .map(SkillRefDTO::from).toList();
        return new CourseDetailDTO(toCard(course, studentId), SubjectCardDTO.from(course.getSubject(), true),
                course.getTagline(), course.getCourseDescription(), course.getQuote(), course.getQuoteAuthor(), skills,
                gameStateService.getCourseRewards(studentId, course.getCourseId()));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseLandingDTO getSubjectLanding(String subjectSlug) {
        Subject subject = subjectsRepository.findBySlug(subjectSlug)
                .filter(StudentVisibility::isVisible)
                .orElseThrow(() -> new NotFoundException("Materia non trovata"));
        List<Course> courses = visibleCourses(subject);
        if (courses.isEmpty()) {
            return new CourseLandingDTO(SubjectCardDTO.from(subject, false), null, 0, 0, List.of());
        }
        // Corso di riferimento: quello in evidenza pubblicato più di recente, altrimenti il primo pubblicato.
        // TODO (Blocco 5): tra i due, "il più popolare" (§5.8)
        OffsetDateTime now = OffsetDateTime.now();
        Course reference = courses.stream()
                .filter(course -> isFeatured(course, now))
                .max(Comparator.comparing(Course::getPublishedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseGet(() -> courses.stream()
                        .min(Comparator.comparing(Course::getPublishedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(courses.get(0)));
        List<Mission> missions = visibleMissions(reference);
        List<LandingStepDTO> steps = new ArrayList<>();
        for (int i = 0; i < Math.min(LANDING_STEPS, missions.size()); i++) {
            Mission mission = missions.get(i);
            steps.add(new LandingStepDTO(i + 1, mission.getMissionTitle(), mission.getTagline(), mission.getCoverReference()));
        }
        // Senza account: nessun progresso e nessun lucchetto personale
        return new CourseLandingDTO(SubjectCardDTO.from(subject, true), toCard(reference, null), missions.size(),
                coursesRepository.countVisibleExercises(reference.getCourseId()), steps);
    }

    // Usato anche dalle missioni: 404 se il corso non esiste o non è visibile
    Course findVisibleCourse(String courseSlug) {
        return coursesRepository.findBySlug(courseSlug)
                .filter(StudentVisibility::isVisible)
                .orElseThrow(() -> new NotFoundException("Corso non trovato"));
    }

    List<Mission> visibleMissions(Course course) {
        return missionsRepository.findByCourse_CourseIdAndStatusOrderByMissionSortOrder(course.getCourseId(), ContentStatus.published);
    }

    private boolean hasCourses(Subject subject) {
        return coursesRepository.existsBySubject_SubjectIdAndStatus(subject.getSubjectId(), ContentStatus.published);
    }

    private List<Course> visibleCourses(Subject subject) {
        return coursesRepository.findBySubject_SubjectIdAndStatusOrderByCourseId(subject.getSubjectId(), ContentStatus.published);
    }

    private static boolean isFeatured(Course course, OffsetDateTime now) {
        return course.getFeaturedUntil() != null && course.getFeaturedUntil().isAfter(now);
    }

    // studentId null = visitatore senza account (landing pubblica)
    private CourseCardDTO toCard(Course course, Long studentId) {
        List<Mission> missions = visibleMissions(course);
        Integer progress = null;
        boolean premiumLocked = false;
        if (studentId != null) {
            long passed = missionAttemptsRepository.findPassedMissionIds(studentId, course.getCourseId()).stream()
                    .filter(id -> missions.stream().anyMatch(mission -> mission.getMissionId().equals(id)))
                    .count();
            // null = non ancora iniziato. TODO (Blocco 5): 0 per un corso iniziato senza missioni superate
            progress = passed == 0 || missions.isEmpty() ? null : (int) (passed * 100 / missions.size());
            premiumLocked = !entitlementService.canAccess(studentId, course);
        }
        return new CourseCardDTO(course.getSlug(), course.getSubject().getSlug(), course.getCourseTitle(),
                course.getCourseShortDescription(), course.getCourseImage(), missions.size(), course.getLevel(),
                (int) coursesRepository.countVisibleSkills(course.getCourseId()), progress,
                isFeatured(course, OffsetDateTime.now()), false, premiumLocked, course.isEntryCourse());
    }
}
