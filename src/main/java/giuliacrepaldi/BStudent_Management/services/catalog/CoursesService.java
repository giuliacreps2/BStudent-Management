package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.catalog.CourseDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.CoursesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ICoursesService;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ISubjectsService;
import giuliacrepaldi.BStudent_Management.utilities.Pageables;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class CoursesService implements ICoursesService {

    private static final Set<String> SORTS = Set.of("courseId", "courseTitle", "status", "publishedAt");

    private final CoursesRepository coursesRepository;
    private final MissionsRepository missionsRepository;
    private final ISubjectsService subjectsService;

    public CoursesService(CoursesRepository coursesRepository, MissionsRepository missionsRepository,
                          ISubjectsService subjectsService) {
        this.coursesRepository = coursesRepository;
        this.missionsRepository = missionsRepository;
        this.subjectsService = subjectsService;
    }

    private static void applyContent(Course course, CourseDTO body) {
        course.setCourseTitle(body.title().strip());
        course.setCourseShortDescription(body.shortDescription().strip());
        course.setCourseDescription(body.extendedDescription().strip());
        course.setCourseImage(body.coverReference() == null || body.coverReference().isBlank()
                ? null : body.coverReference().strip());
        course.setAccessTier(body.accessTier());
    }

    @Override
    @Transactional
    public Course saveNewCourse(long subjectId, CourseDTO body) {
        Subject subject = subjectsService.findById(subjectId);
        Course course = Course.builder()
                .subject(subject)
                .status(ContentStatus.draft)
                .build();
        applyContent(course, body);
        return coursesRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Course findById(long courseId) {
        return coursesRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> findAll(int page, int size, String sortBy) {
        return coursesRepository.findAll(Pageables.of(page, size, sortBy, SORTS, "courseTitle"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> findBySubject(long subjectId, int page, int size, String sortBy) {
        // 404 se la materia non esiste, invece di una pagina vuota
        subjectsService.findById(subjectId);
        return coursesRepository.findBySubject_SubjectId(subjectId,
                Pageables.of(page, size, sortBy, SORTS, "courseTitle"));
    }

    @Override
    @Transactional
    public Course findByIdAndUpdate(long courseId, CourseDTO body) {
        Course course = findById(courseId);
        // accessTier è solo il default delle missioni: cambiarlo non tocca quelle con un livello proprio.
        // Ammessa in ogni stato: i contenuti pubblicati mostrano gli aggiornamenti (blueprint §5.3)
        applyContent(course, body);
        return course;
    }

    @Override
    @Transactional
    public Course publishCourse(long courseId) {
        Course course = findById(courseId);
        if (course.getStatus() == ContentStatus.published) {
            throw new ConflictException("Il corso è già pubblicato");
        }
        // Un corso visibile ma senza missioni pubblicate sarebbe vuoto per lo studente
        if (!missionsRepository.existsByCourse_CourseIdAndStatus(courseId, ContentStatus.published)) {
            throw new ConflictException("Il corso non ha missioni pubblicate");
        }
        course.setStatus(ContentStatus.published);
        // published_at è la prima pubblicazione: resta anche dopo un ritiro e un ripristino
        if (course.getPublishedAt() == null) {
            course.setPublishedAt(OffsetDateTime.now());
        }
        course.setWithdrawnAt(null);
        return course;
    }

    @Override
    @Transactional
    public Course withdrawCourse(long courseId) {
        Course course = findById(courseId);
        if (course.getStatus() != ContentStatus.published) {
            throw new ConflictException("Si può ritirare solo un corso pubblicato");
        }
        // Nasconde il corso; tentativi e progressi restano per lo storico
        course.setStatus(ContentStatus.withdrawn);
        course.setWithdrawnAt(OffsetDateTime.now());
        return course;
    }

    @Override
    @Transactional
    public void deleteCourse(long courseId) {
        Course course = findById(courseId);
        if (course.getStatus() != ContentStatus.draft || course.getPublishedAt() != null) {
            throw new ConflictException("Un corso già pubblicato non si elimina: va ritirato");
        }
        if (missionsRepository.existsByCourse_CourseId(courseId)) {
            throw new ConflictException("Il corso ha delle missioni: eliminale prima");
        }
        coursesRepository.delete(course);
    }
}
