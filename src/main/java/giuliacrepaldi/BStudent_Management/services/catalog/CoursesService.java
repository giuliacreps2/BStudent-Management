package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
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
import giuliacrepaldi.BStudent_Management.utilities.Slugs;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

@Service
public class CoursesService implements ICoursesService {

    private static final Set<String> SORTS = Set.of("courseId", "courseTitle", "status", "publishedAt", "createdAt", "updatedAt");

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
        course.setCourseImage(blankToNull(body.coverReference()));
        course.setAccessTier(body.accessTier());
        course.setTagline(blankToNull(body.tagline()));
        course.setLevel(body.level());
        course.setFeaturedUntil(body.featuredUntil());
        course.setWorldTheme(blankToNull(body.worldTheme()));
        String quote = blankToNull(body.quote());
        course.setQuote(quote);
        // L'autore ha senso solo con la citazione (chk_courses_quote)
        course.setQuoteAuthor(quote == null ? null : blankToNull(body.quoteAuthor()));
    }

    // Corso d'ingresso (Blueprint Gamification §8-bis): uno per materia, gratuito dall'inizio alla fine.
    // Dà la prima evoluzione a chiunque, quindi non può contenere nulla a pagamento
    private void applyEntryCourse(Course course, CourseDTO body) {
        boolean entry = Boolean.TRUE.equals(body.entryCourse());
        if (entry) {
            if (body.accessTier() != AccessTier.free) {
                throw new BadRequestException("Il corso d'ingresso deve essere gratuito: imposta il corso come gratuito");
            }
            Long subjectId = course.getSubject().getSubjectId();
            coursesRepository.findBySubject_SubjectIdAndEntryCourseTrue(subjectId)
                    .filter(other -> !other.getCourseId().equals(course.getCourseId()))
                    .ifPresent(other -> {
                        throw new ConflictException("La materia ha già un corso d'ingresso: «" + other.getCourseTitle()
                                + "». Togli prima la spunta da quel corso");
                    });
            if (course.getCourseId() != null) {
                long premium = missionsRepository.countByCourse_CourseIdAndAccessTier(course.getCourseId(), AccessTier.premium);
                if (premium > 0) {
                    throw new ConflictException("Il corso ha " + premium + (premium == 1 ? " missione" : " missioni")
                            + " a pagamento: rendile gratuite prima di farne il corso d'ingresso");
                }
            }
        }
        course.setEntryCourse(entry);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    @Override
    @Transactional
    public Course saveNewCourse(long subjectId, CourseDTO body) {
        Subject subject = subjectsService.findById(subjectId);
        String slug = Slugs.resolve(body.slug(), body.title());
        if (coursesRepository.existsBySlug(slug)) {
            throw new ConflictException("Esiste già un corso con l'indirizzo " + slug);
        }
        Course course = Course.builder()
                .subject(subject)
                .slug(slug)
                .status(ContentStatus.draft)
                .build();
        applyContent(course, body);
        applyEntryCourse(course, body);
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
        String slug = Slugs.resolve(body.slug(), body.title());
        if (coursesRepository.existsBySlugAndCourseIdNot(slug, courseId)) {
            throw new ConflictException("Esiste già un corso con l'indirizzo " + slug);
        }
        // Un corso pubblicato non torna senza livello: il test di livello lo usa
        if (course.getStatus() == ContentStatus.published && body.level() == null) {
            throw new ConflictException("Un corso pubblicato deve avere un livello");
        }
        course.setSlug(slug);
        // accessTier è solo il default delle missioni: cambiarlo non tocca quelle con un livello proprio.
        // Ammessa in ogni stato: i contenuti pubblicati mostrano gli aggiornamenti (blueprint §5.3)
        applyContent(course, body);
        applyEntryCourse(course, body);
        return course;
    }

    @Override
    @Transactional
    public Course publishCourse(long courseId) {
        Course course = findById(courseId);
        if (course.getStatus() == ContentStatus.published) {
            throw new ConflictException("Il corso è già pubblicato");
        }
        if (course.getLevel() == null) {
            throw new ConflictException("Scegli il livello del corso prima di pubblicarlo");
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
