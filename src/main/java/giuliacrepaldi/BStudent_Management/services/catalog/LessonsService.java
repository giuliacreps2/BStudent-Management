package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.LessonPage;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonPageDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonPagesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExercisesRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ILessonsService;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.IMissionsService;
import giuliacrepaldi.BStudent_Management.utilities.Reorders;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LessonsService implements ILessonsService {

    private static final String LESSON_ORDER_CONSTRAINT = "lessons_mission_id_sort_order_idx";
    private static final String PAGE_ORDER_CONSTRAINT = "lesson_pages_lesson_id_sort_order_idx";

    private final LessonsRepository lessonsRepository;
    private final LessonPagesRepository lessonPagesRepository;
    private final MissionExercisesRepository missionExercisesRepository;
    private final IMissionsService missionsService;
    private final EntityManager entityManager;

    public LessonsService(LessonsRepository lessonsRepository, LessonPagesRepository lessonPagesRepository,
                          MissionExercisesRepository missionExercisesRepository, IMissionsService missionsService,
                          EntityManager entityManager) {
        this.lessonsRepository = lessonsRepository;
        this.lessonPagesRepository = lessonPagesRepository;
        this.missionExercisesRepository = missionExercisesRepository;
        this.missionsService = missionsService;
        this.entityManager = entityManager;
    }

    //LEZIONI

    @Override
    @Transactional
    public Lesson saveNewLesson(long missionId, LessonDTO body) {
        Mission mission = missionsService.findById(missionId);
        // In coda: due creazioni contemporanee sulla stessa missione violano l'unicità -> 409
        Lesson lesson = Lesson.builder()
                .mission(mission)
                .lessonTitle(body.title().strip())
                .lessonSortOrder(lessonsRepository.findMaxSortOrder(missionId) + 1)
                .status(ContentStatus.draft)
                .build();
        return lessonsRepository.save(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public Lesson findById(long lessonId) {
        return lessonsRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException(lessonId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lesson> findByMission(long missionId) {
        // 404 se la missione non esiste, invece di una lista vuota
        missionsService.findById(missionId);
        return lessonsRepository.findByMission_MissionIdOrderByLessonSortOrder(missionId);
    }

    @Override
    @Transactional
    public Lesson findByIdAndUpdate(long lessonId, LessonDTO body) {
        Lesson lesson = findById(lessonId);
        // Ammessa in ogni stato: il Taccuino mostra gli aggiornamenti senza perdere le note (blueprint §14)
        lesson.setLessonTitle(body.title().strip());
        return lesson;
    }

    @Override
    @Transactional
    public List<Lesson> reorderLessons(long missionId, List<Long> lessonIds) {
        Map<Long, Lesson> byId = findByMission(missionId).stream()
                .collect(Collectors.toMap(Lesson::getLessonId, Function.identity()));
        Reorders.validateIds(lessonIds, byId.keySet(), "le lezioni della missione");
        Reorders.deferUniqueOrder(entityManager, LESSON_ORDER_CONSTRAINT);

        // L'ordine delle lezioni è anche l'ordine degli esercizi della missione (blueprint §9.1)
        List<Lesson> reordered = new ArrayList<>(lessonIds.size());
        for (int i = 0; i < lessonIds.size(); i++) {
            Lesson lesson = byId.get(lessonIds.get(i));
            lesson.setLessonSortOrder(i + 1);
            reordered.add(lesson);
        }
        return reordered;
    }

    @Override
    @Transactional
    public Lesson publishLesson(long lessonId) {
        Lesson lesson = findById(lessonId);
        if (lesson.getStatus() == ContentStatus.published) {
            throw new ConflictException("La lezione è già pubblicata");
        }
        if (lessonPagesRepository.countByLesson_LessonId(lessonId) == 0) {
            throw new ConflictException("La lezione non ha pagine");
        }
        // Ogni lezione fruibile è collegata a un esercizio (blueprint §5.1)
        if (!missionExercisesRepository.existsByLesson_LessonId(lessonId)) {
            throw new ConflictException("La lezione non ha un esercizio collegato");
        }
        lesson.setStatus(ContentStatus.published);
        // published_at è la prima pubblicazione: resta anche dopo un ritiro e un ripristino
        if (lesson.getPublishedAt() == null) {
            lesson.setPublishedAt(OffsetDateTime.now());
        }
        lesson.setWithdrawnAt(null);
        return lesson;
    }

    @Override
    @Transactional
    public Lesson withdrawLesson(long lessonId) {
        Lesson lesson = findById(lessonId);
        if (lesson.getStatus() != ContentStatus.published) {
            throw new ConflictException("Si può ritirare solo una lezione pubblicata");
        }
        // Una missione pubblicata deve avere almeno una lezione pubblicata
        Mission mission = lesson.getMission();
        if (mission.getStatus() == ContentStatus.published
                && lessonsRepository.countByMission_MissionIdAndStatus(mission.getMissionId(), ContentStatus.published) == 1) {
            throw new ConflictException("È l'ultima lezione pubblicata della missione: ritira prima la missione");
        }
        // Ritirata sparisce anche dal Taccuino (blueprint §5.3): lo filtrerà la lettura del Taccuino
        lesson.setStatus(ContentStatus.withdrawn);
        lesson.setWithdrawnAt(OffsetDateTime.now());
        return lesson;
    }

    @Override
    @Transactional
    public void deleteLesson(long lessonId) {
        Lesson lesson = findById(lessonId);
        if (lesson.getStatus() != ContentStatus.draft || lesson.getPublishedAt() != null) {
            throw new ConflictException("Una lezione già pubblicata non si elimina: va ritirata");
        }
        if (missionExercisesRepository.existsByLesson_LessonId(lessonId)) {
            throw new ConflictException("La lezione ha un esercizio collegato: scollegalo prima");
        }
        // Le pagine non hanno storico: vanno via con la lezione
        lessonPagesRepository.deleteByLessonId(lessonId);
        lessonsRepository.delete(lesson);
    }

    //PAGINE

    @Override
    @Transactional
    public LessonPage saveNewLessonPage(long lessonId, LessonPageDTO body) {
        Lesson lesson = findById(lessonId);
        LessonPage page = LessonPage.builder()
                .lesson(lesson)
                .lessonPageSortOrder(lessonPagesRepository.findMaxSortOrder(lessonId) + 1)
                .build();
        applyContent(page, body);
        return lessonPagesRepository.save(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonPage> findPagesByLesson(long lessonId) {
        findById(lessonId);
        return lessonPagesRepository.findByLesson_LessonIdOrderByLessonPageSortOrder(lessonId);
    }

    @Override
    @Transactional
    public LessonPage findPageByIdAndUpdate(long lessonPageId, LessonPageDTO body) {
        LessonPage page = findPageById(lessonPageId);
        // Sostituisce il contenuto: si può anche cambiare tipo (es. da testo a immagine)
        applyContent(page, body);
        return page;
    }

    @Override
    @Transactional
    public List<LessonPage> reorderLessonPages(long lessonId, List<Long> lessonPageIds) {
        Map<Long, LessonPage> byId = findPagesByLesson(lessonId).stream()
                .collect(Collectors.toMap(LessonPage::getLessonPageId, Function.identity()));
        Reorders.validateIds(lessonPageIds, byId.keySet(), "le pagine della lezione");
        Reorders.deferUniqueOrder(entityManager, PAGE_ORDER_CONSTRAINT);

        List<LessonPage> reordered = new ArrayList<>(lessonPageIds.size());
        for (int i = 0; i < lessonPageIds.size(); i++) {
            LessonPage page = byId.get(lessonPageIds.get(i));
            page.setLessonPageSortOrder(i + 1);
            reordered.add(page);
        }
        return reordered;
    }

    @Override
    @Transactional
    public void deleteLessonPage(long lessonPageId) {
        LessonPage page = findPageById(lessonPageId);
        Lesson lesson = page.getLesson();
        // Una lezione pubblicata non resta vuota
        if (lesson.getStatus() == ContentStatus.published
                && lessonPagesRepository.countByLesson_LessonId(lesson.getLessonId()) == 1) {
            throw new ConflictException("È l'unica pagina di una lezione pubblicata: aggiungine un'altra o ritira la lezione");
        }
        // Il buco nell'ordine (es. 1, 3) non viola il vincolo: si sistema con il riordino
        lessonPagesRepository.delete(page);
    }

    private LessonPage findPageById(long lessonPageId) {
        return lessonPagesRepository.findById(lessonPageId)
                .orElseThrow(() -> new NotFoundException(lessonPageId));
    }

    // Esattamente un contenuto; il tipo (kind) lo ricava l'entity. Validato qui per rispondere 400
    // invece del 500 dell'IllegalStateException nell'entity
    private static void applyContent(LessonPage page, LessonPageDTO body) {
        String text = blankToNull(body.formattedText());
        String image = blankToNull(body.imageReference());
        String provider = blankToNull(body.videoProvider());
        String identifier = blankToNull(body.videoIdentifier());

        int contents = (text != null ? 1 : 0) + (image != null ? 1 : 0)
                + (provider != null || identifier != null ? 1 : 0);
        if (contents != 1) {
            throw new BadRequestException("Una pagina contiene esattamente un contenuto: testo, immagine o video");
        }
        if ((provider == null) != (identifier == null)) {
            throw new BadRequestException("Il video richiede sia videoProvider sia videoIdentifier");
        }

        page.setFormattedText(text);
        page.setImageReference(image);
        page.setVideoProvider(provider);
        page.setVideoIdentifier(identifier);
    }

    // Il testo formattato non si accorcia: gli spazi possono far parte della formattazione
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
