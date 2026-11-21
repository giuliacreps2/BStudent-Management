package giuliacrepaldi.BStudent_Management.services.notebook;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.LessonPage;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.notebook.LessonNote;
import giuliacrepaldi.BStudent_Management.entities.notebook.NotebookLesson;
import giuliacrepaldi.BStudent_Management.entities.notebook.ResourceBookmark;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.Kind;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonPageResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.LessonNoteDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.LessonNoteResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.NotebookItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.NotebookLessonDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.NotebookOverviewDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.ResourceCardDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonPagesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.notebook.LessonNotesRepository;
import giuliacrepaldi.BStudent_Management.repositories.notebook.NotebookLessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.notebook.ResourceBookmarksRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.StudyEventsRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import giuliacrepaldi.BStudent_Management.services.notebook.interfaces.INotebookService;
import giuliacrepaldi.BStudent_Management.services.student.StudentVisibility;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotebookService implements INotebookService {

    /** Lunghezza massima dell'anteprima: il primo paragrafo, tagliato a parola intera */
    static final int PREVIEW_LENGTH = 220;

    private final NotebookLessonsRepository notebookLessonsRepository;
    private final LessonNotesRepository notesRepository;
    private final LessonsRepository lessonsRepository;
    private final LessonPagesRepository lessonPagesRepository;
    private final StudentsRepository studentsRepository;
    private final ResourceBookmarksRepository bookmarksRepository;
    private final ResourcesRepository resourcesRepository;
    private final StudyEventsRepository studyEventsRepository;
    private final IEntitlementService entitlementService;
    private final EntityManager entityManager;

    public NotebookService(NotebookLessonsRepository notebookLessonsRepository, LessonNotesRepository notesRepository,
                           LessonsRepository lessonsRepository, LessonPagesRepository lessonPagesRepository,
                           StudentsRepository studentsRepository, ResourceBookmarksRepository bookmarksRepository,
                           ResourcesRepository resourcesRepository, StudyEventsRepository studyEventsRepository,
                           IEntitlementService entitlementService, EntityManager entityManager) {
        this.notebookLessonsRepository = notebookLessonsRepository;
        this.notesRepository = notesRepository;
        this.lessonsRepository = lessonsRepository;
        this.lessonPagesRepository = lessonPagesRepository;
        this.studentsRepository = studentsRepository;
        this.bookmarksRepository = bookmarksRepository;
        this.resourcesRepository = resourcesRepository;
        this.studyEventsRepository = studyEventsRepository;
        this.entitlementService = entitlementService;
        this.entityManager = entityManager;
    }

    // La visibilità della lezione la controlla chi chiama (lo svolgimento: è il passo attuale della missione)
    @Override
    @Transactional
    public void acquireLesson(long studentId, long lessonId) {
        NotebookLesson.NotebookLessonId id = new NotebookLesson.NotebookLessonId(studentId, lessonId);
        if (notebookLessonsRepository.existsById(id)) {
            return;
        }
        // persist e non save: con la chiave composta già valorizzata save farebbe un merge
        entityManager.persist(NotebookLesson.builder().id(id)
                .student(studentsRepository.getReferenceById(studentId))
                .lesson(lessonsRepository.getReferenceById(lessonId))
                .build());
    }

    // ------------------------------------------------------------------ Lettura

    @Override
    @Transactional(readOnly = true)
    public NotebookOverviewDTO getOverview(long studentId) {
        Student student = findStudent(studentId);
        Map<Long, LessonNote> notes = notesRepository.findById_StudentId(studentId).stream()
                .collect(Collectors.toMap(n -> n.getId().getLessonId(), Function.identity()));
        Set<Long> reopened = new HashSet<>(studyEventsRepository.findReopenedNotebookLessonIds(studentId));

        // Linguette nell'ordine della lezione più recente di ogni materia
        Map<Long, NotebookOverviewDTO.Tab> tabs = new LinkedHashMap<>();
        for (NotebookLesson entry : notebookLessonsRepository.findByStudent(studentId)) {
            NotebookItemDTO item = toItem(studentId, entry, notes.get(entry.getLesson().getLessonId()), reopened);
            if (item == null) {
                continue;
            }
            Subject subject = entry.getLesson().getMission().getCourse().getSubject();
            tabs.computeIfAbsent(subject.getSubjectId(),
                    id -> new NotebookOverviewDTO.Tab(subject.getSlug(), subject.getSubjectName(), new ArrayList<>()))
                    .items().add(item);
        }
        return new NotebookOverviewDTO(student.getNotebookCoverColor(), List.copyOf(tabs.values()), bookmarks(studentId));
    }

    @Override
    @Transactional(readOnly = true)
    public NotebookLessonDTO getLesson(long studentId, long lessonId) {
        NotebookLesson entry = findEntry(studentId, lessonId);
        LessonNote note = notesRepository.findById(new LessonNote.LessonNoteId(studentId, lessonId)).orElse(null);
        NotebookItemDTO item = toItem(studentId, entry, note,
                new HashSet<>(studyEventsRepository.findReopenedNotebookLessonIds(studentId)));
        if (item == null) {
            throw new NotFoundException("Lezione non trovata nel Quaderno");
        }
        List<LessonPageResponseDTO> pages = item.available() && !item.premiumLocked()
                ? lessonPagesRepository.findByLesson_LessonIdOrderByLessonPageSortOrder(lessonId).stream()
                        .map(LessonPageResponseDTO::from).toList()
                : List.of();
        return new NotebookLessonDTO(item, pages, note == null ? null : LessonNoteResponseDTO.from(note));
    }

    /**
     * Voce del Quaderno, oppure null se non va mostrata: lezione ritirata (o tolta dalla missione visibile)
     * senza nota dello studente (blueprint §14, deciso il 14/11/2026)
     */
    private NotebookItemDTO toItem(long studentId, NotebookLesson entry, LessonNote note, Set<Long> reopened) {
        Lesson lesson = entry.getLesson();
        Mission mission = lesson.getMission();
        boolean available = lesson.getStatus() == ContentStatus.published && StudentVisibility.isVisible(mission);
        if (!available && note == null) {
            return null;
        }
        boolean premiumLocked = available && !entitlementService.canAccess(studentId, mission);
        List<LessonPage> pages = available
                ? lessonPagesRepository.findByLesson_LessonIdOrderByLessonPageSortOrder(lesson.getLessonId())
                : List.of();
        return new NotebookItemDTO(lesson.getLessonId(), kindOf(pages), lesson.getLessonTitle(),
                lesson.getTopic() == null ? null : lesson.getTopic().getTopicId(),
                lesson.getTopic() == null ? null : lesson.getTopic().getTopicName(),
                available ? preview(pages) : null, entry.getAcquiredAt(),
                mission.getCourse().getSlug(), mission.getCourse().getCourseTitle(), mission.getSlug(),
                mission.getMissionTitle(), available, premiumLocked,
                !reopened.contains(lesson.getLessonId()), note != null);
    }

    /** "video" se la lezione è fatta solo di video, altrimenti "lesson" */
    static String kindOf(List<LessonPage> pages) {
        return !pages.isEmpty() && pages.stream().allMatch(p -> p.getKind() == Kind.video) ? "video" : "lesson";
    }

    /** Primo paragrafo della prima pagina di testo, tagliato a parola intera */
    static String preview(List<LessonPage> pages) {
        String text = pages.stream().filter(p -> p.getKind() == Kind.text && p.getFormattedText() != null)
                .map(LessonPage::getFormattedText).findFirst().orElse(null);
        if (text == null) {
            return null;
        }
        String paragraph = text.strip().split("\\R\\s*\\R", 2)[0].replaceAll("\\s+", " ").strip();
        if (paragraph.length() <= PREVIEW_LENGTH) {
            return paragraph;
        }
        int cut = paragraph.lastIndexOf(' ', PREVIEW_LENGTH);
        return paragraph.substring(0, cut > 0 ? cut : PREVIEW_LENGTH) + "…";
    }

    private List<ResourceCardDTO> bookmarks(long studentId) {
        List<Long> ids = bookmarksRepository.findById_StudentIdOrderBySavedAtDesc(studentId).stream()
                .map(b -> b.getId().getResourceId()).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        boolean premium = entitlementService.hasPremium(studentId);
        Map<Long, Resource> resources = resourcesRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Resource::getResourceId, Function.identity()));
        // Una Risorsa ritirata sparisce dai segnalibri (come un contenuto ritirato dal Quaderno)
        return ids.stream().map(resources::get)
                .filter(r -> r != null && StudentVisibility.isVisible(r))
                .map(r -> ResourceCardDTO.from(r, r.getAccessTier() == AccessTier.premium && !premium))
                .toList();
    }

    // ------------------------------------------------------------------ Nota

    @Override
    @Transactional
    public LessonNoteResponseDTO saveNote(long studentId, long lessonId, LessonNoteDTO body) {
        NotebookLesson entry = findEntry(studentId, lessonId);
        LessonNote.LessonNoteId id = new LessonNote.LessonNoteId(studentId, lessonId);
        LessonNote note = notesRepository.findById(id).orElse(null);
        String text = body.text() == null ? "" : body.text().strip();

        if (note != null && (body.version() == null || body.version() != note.getVersion())) {
            throw new ConflictException("La nota è stata modificata in un'altra scheda: ricarica la pagina per vederla");
        }
        if (text.isEmpty()) {
            if (note != null) {
                notesRepository.delete(note);
            }
            return null;
        }
        if (note == null) {
            note = LessonNote.builder().id(id)
                    .student(entry.getStudent())
                    .lesson(entry.getLesson())
                    .noteText(text)
                    .build();
            entityManager.persist(note);
        } else {
            note.setNoteText(text);
            note.setVersion(note.getVersion() + 1);
        }
        entityManager.flush();
        return LessonNoteResponseDTO.from(note);
    }

    @Override
    @Transactional
    public void deleteNote(long studentId, long lessonId) {
        findEntry(studentId, lessonId);
        notesRepository.findById(new LessonNote.LessonNoteId(studentId, lessonId)).ifPresent(notesRepository::delete);
    }

    // ------------------------------------------------------------------ Copertina e segnalibri

    @Override
    @Transactional
    public void setCoverColor(long studentId, String color) {
        findStudent(studentId).setNotebookCoverColor(color == null || color.isBlank() ? null : color.strip());
    }

    @Override
    @Transactional
    public void addBookmark(long studentId, long resourceId) {
        resourcesRepository.findById(resourceId)
                .filter(StudentVisibility::isVisible)
                .orElseThrow(() -> new NotFoundException("Risorsa non trovata"));
        ResourceBookmark.ResourceBookmarkId id = new ResourceBookmark.ResourceBookmarkId(studentId, resourceId);
        if (bookmarksRepository.existsById(id)) {
            return;
        }
        entityManager.persist(ResourceBookmark.builder().id(id).savedAt(OffsetDateTime.now()).build());
    }

    @Override
    @Transactional
    public void removeBookmark(long studentId, long resourceId) {
        bookmarksRepository.deleteById(new ResourceBookmark.ResourceBookmarkId(studentId, resourceId));
    }

    // ------------------------------------------------------------------ Utilità

    private NotebookLesson findEntry(long studentId, long lessonId) {
        return notebookLessonsRepository.findById(new NotebookLesson.NotebookLessonId(studentId, lessonId))
                .orElseThrow(() -> new NotFoundException("Lezione non trovata nel Quaderno"));
    }

    private Student findStudent(long studentId) {
        return studentsRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Studente non trovato"));
    }
}
