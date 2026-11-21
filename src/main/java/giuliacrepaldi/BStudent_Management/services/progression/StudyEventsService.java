package giuliacrepaldi.BStudent_Management.services.progression;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.LessonPage;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.progression.StudyEvent;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.progression.StudyEventKind;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.progression.StudyEventDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonPagesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.StudyEventsRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.IStudyEventsService;
import giuliacrepaldi.BStudent_Management.services.student.StudentVisibility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;

@Service
public class StudyEventsService implements IStudyEventsService {

    /** Differenza tollerata fra l'orologio del dispositivo e quello del server */
    static final Duration CLOCK_TOLERANCE = Duration.ofMinutes(2);

    private final StudyEventsRepository eventsRepository;
    private final StudentsRepository studentsRepository;
    private final LessonsRepository lessonsRepository;
    private final LessonPagesRepository lessonPagesRepository;
    private final ResourcesRepository resourcesRepository;
    private final PracticeExercisesRepository practiceExercisesRepository;

    public StudyEventsService(StudyEventsRepository eventsRepository, StudentsRepository studentsRepository,
                              LessonsRepository lessonsRepository, LessonPagesRepository lessonPagesRepository,
                              ResourcesRepository resourcesRepository,
                              PracticeExercisesRepository practiceExercisesRepository) {
        this.eventsRepository = eventsRepository;
        this.studentsRepository = studentsRepository;
        this.lessonsRepository = lessonsRepository;
        this.lessonPagesRepository = lessonPagesRepository;
        this.resourcesRepository = resourcesRepository;
        this.practiceExercisesRepository = practiceExercisesRepository;
    }

    @Override
    @Transactional
    public void record(long studentId, StudyEventDTO body) {
        OffsetDateTime now = OffsetDateTime.now();
        StudyEvent.StudyEventBuilder event = StudyEvent.builder()
                .student(studentsRepository.getReferenceById(studentId))
                .kind(body.kind());

        if (body.kind() == StudyEventKind.recommendation_open) {
            PracticeExercise exercise = practiceExercisesRepository.findById(required(body.practiceExerciseId(), "la voce"))
                    .filter(StudentVisibility::isVisible)
                    .orElseThrow(() -> new NotFoundException("Esercizio non trovato"));
            eventsRepository.save(event.practiceExercise(exercise).startedAt(now).activeMs(null).build());
            return;
        }

        OffsetDateTime startedAt = checkReading(body, now);
        if (body.kind() == StudyEventKind.lesson_read) {
            Lesson lesson = lessonsRepository.findById(required(body.lessonId(), "la lezione"))
                    .filter(l -> l.getStatus() == ContentStatus.published && StudentVisibility.isVisible(l.getMission()))
                    .orElseThrow(() -> new NotFoundException("Lezione non trovata"));
            LessonPage page = null;
            if (body.lessonPageId() != null) {
                page = lessonPagesRepository.findById(body.lessonPageId())
                        .filter(p -> p.getLesson().getLessonId().equals(lesson.getLessonId()))
                        .orElseThrow(() -> new NotFoundException("Pagina della lezione non trovata"));
            }
            event.lesson(lesson).lessonPage(page);
        } else {
            Resource resource = resourcesRepository.findById(required(body.resourceId(), "la Risorsa"))
                    .filter(StudentVisibility::isVisible)
                    .orElseThrow(() -> new NotFoundException("Risorsa non trovata"));
            event.resource(resource);
        }
        eventsRepository.save(event.startedAt(startedAt).activeMs(body.activeMs()).build());
    }

    /** Un tratto di lettura: inizio non nel futuro, tempo attivo positivo e non più lungo del tempo trascorso */
    static OffsetDateTime checkReading(StudyEventDTO body, OffsetDateTime now) {
        if (body.startedAt() == null || body.activeMs() == null || body.activeMs() <= 0) {
            throw new BadRequestException("Una lettura richiede inizio e tempo attivo");
        }
        if (body.startedAt().isAfter(now.plus(CLOCK_TOLERANCE))) {
            throw new BadRequestException("L'inizio della lettura è nel futuro");
        }
        long elapsed = Duration.between(body.startedAt(), now).plus(CLOCK_TOLERANCE).toMillis();
        if (body.activeMs() > elapsed) {
            throw new BadRequestException("Il tempo attivo è più lungo del tempo trascorso");
        }
        return body.startedAt();
    }

    private static long required(Long id, String what) {
        if (id == null) {
            throw new BadRequestException("Manca " + what);
        }
        return id;
    }
}
