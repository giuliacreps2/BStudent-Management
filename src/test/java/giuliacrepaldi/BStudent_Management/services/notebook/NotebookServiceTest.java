package giuliacrepaldi.BStudent_Management.services.notebook;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.LessonPage;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.notebook.LessonNote;
import giuliacrepaldi.BStudent_Management.entities.notebook.NotebookLesson;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.Kind;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.payloads.notebook.LessonNoteDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.LessonNoteResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.NotebookItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.notebook.NotebookOverviewDTO;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonPagesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.notebook.LessonNotesRepository;
import giuliacrepaldi.BStudent_Management.repositories.notebook.NotebookLessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.notebook.ResourceBookmarksRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.StudyEventsRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Quaderno (blueprint §14)
class NotebookServiceTest {

    private static final long STUDENT = 7L;

    private NotebookLessonsRepository notebookLessons;
    private LessonNotesRepository notes;
    private LessonPagesRepository pages;
    private StudyEventsRepository events;
    private IEntitlementService entitlements;
    private NotebookService service;
    private Student student;

    @BeforeEach
    void setUp() {
        notebookLessons = mock(NotebookLessonsRepository.class);
        notes = mock(LessonNotesRepository.class);
        pages = mock(LessonPagesRepository.class);
        events = mock(StudyEventsRepository.class);
        entitlements = mock(IEntitlementService.class);
        StudentsRepository students = mock(StudentsRepository.class);
        student = new Student();
        student.setStudentId(STUDENT);
        student.setNotebookCoverColor("verde");
        when(students.findById(STUDENT)).thenReturn(Optional.of(student));
        when(entitlements.canAccess(anyLong(), any(Mission.class))).thenReturn(true);
        when(events.findReopenedNotebookLessonIds(STUDENT)).thenReturn(List.of());
        when(notes.findById_StudentId(STUDENT)).thenReturn(List.of());
        ResourceBookmarksRepository bookmarks = mock(ResourceBookmarksRepository.class);
        when(bookmarks.findById_StudentIdOrderBySavedAtDesc(STUDENT)).thenReturn(List.of());
        service = new NotebookService(notebookLessons, notes, mock(LessonsRepository.class), pages, students,
                bookmarks, mock(ResourcesRepository.class), events, entitlements, mock(EntityManager.class));
    }

    private NotebookLesson entry(long lessonId, ContentStatus status) {
        Subject subject = new Subject();
        subject.setSubjectId(2L);
        subject.setSlug("latino");
        subject.setSubjectName("Latino");
        subject.setStatus(SubjectStatus.published);
        Course course = new Course();
        course.setSlug("corso");
        course.setCourseTitle("Corso");
        course.setStatus(ContentStatus.published);
        course.setSubject(subject);
        Mission mission = new Mission();
        mission.setSlug("missione");
        mission.setMissionTitle("Missione");
        mission.setStatus(ContentStatus.published);
        mission.setCourse(course);
        Lesson lesson = new Lesson();
        lesson.setLessonId(lessonId);
        lesson.setLessonTitle("Lezione " + lessonId);
        lesson.setStatus(status);
        lesson.setMission(mission);
        NotebookLesson entry = NotebookLesson.builder()
                .id(new NotebookLesson.NotebookLessonId(STUDENT, lessonId))
                .student(student).lesson(lesson).acquiredAt(OffsetDateTime.now()).build();
        when(notebookLessons.findById(new NotebookLesson.NotebookLessonId(STUDENT, lessonId))).thenReturn(Optional.of(entry));
        return entry;
    }

    private static LessonPage page(Kind kind, String text) {
        LessonPage page = new LessonPage();
        page.setKind(kind);
        page.setFormattedText(text);
        return page;
    }

    private static LessonNote note(long lessonId, long version) {
        return LessonNote.builder().id(new LessonNote.LessonNoteId(STUDENT, lessonId))
                .noteText("La mia nota").version(version).updatedAt(OffsetDateTime.now()).build();
    }

    @Test
    void laLezioneRitirataSenzaNotaSparisceConLaNotaRestaNonDisponibile() {
        NotebookLesson withdrawnNoNote = entry(1L, ContentStatus.withdrawn);
        NotebookLesson withdrawnWithNote = entry(2L, ContentStatus.withdrawn);
        NotebookLesson available = entry(3L, ContentStatus.published);
        when(notebookLessons.findByStudent(STUDENT)).thenReturn(List.of(withdrawnNoNote, withdrawnWithNote, available));
        when(notes.findById_StudentId(STUDENT)).thenReturn(List.of(note(2L, 1)));
        when(pages.findByLesson_LessonIdOrderByLessonPageSortOrder(3L))
                .thenReturn(List.of(page(Kind.text, "Primo paragrafo.\n\nSecondo paragrafo.")));

        NotebookOverviewDTO overview = service.getOverview(STUDENT);

        assertThat(overview.coverColor()).isEqualTo("verde");
        assertThat(overview.subjects()).hasSize(1);
        List<NotebookItemDTO> items = overview.subjects().get(0).items();
        assertThat(items).extracting(NotebookItemDTO::lessonId).containsExactly(2L, 3L);
        assertThat(items.get(0).available()).isFalse();
        assertThat(items.get(0).hasNote()).isTrue();
        assertThat(items.get(0).preview()).isNull();
        assertThat(items.get(1).available()).isTrue();
        assertThat(items.get(1).preview()).isEqualTo("Primo paragrafo.");
        assertThat(items.get(1).isNew()).isTrue();
    }

    @Test
    void conLAbbonamentoScadutoLaLezionePremiumHaIlLucchettoMaResta() {
        NotebookLesson entry = entry(3L, ContentStatus.published);
        when(notebookLessons.findByStudent(STUDENT)).thenReturn(List.of(entry));
        when(entitlements.canAccess(eq(STUDENT), any(Mission.class))).thenReturn(false);
        when(events.findReopenedNotebookLessonIds(STUDENT)).thenReturn(List.of(3L));

        NotebookItemDTO item = service.getOverview(STUDENT).subjects().get(0).items().get(0);

        assertThat(item.available()).isTrue();
        assertThat(item.premiumLocked()).isTrue();
        assertThat(item.isNew()).isFalse();
    }

    @Test
    void laNotaNonSovrascriveUnaVersionePiuRecenteDiUnAltraScheda() {
        entry(3L, ContentStatus.published);
        LessonNote existing = note(3L, 2);
        when(notes.findById(new LessonNote.LessonNoteId(STUDENT, 3L))).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.saveNote(STUDENT, 3L, new LessonNoteDTO("Testo vecchio", 1L)))
                .isInstanceOf(ConflictException.class);

        LessonNoteResponseDTO saved = service.saveNote(STUDENT, 3L, new LessonNoteDTO("  Testo nuovo  ", 2L));
        assertThat(saved.text()).isEqualTo("Testo nuovo");
        assertThat(saved.version()).isEqualTo(3L);
    }

    @Test
    void ilTestoVuotoCancellaLaNota() {
        entry(3L, ContentStatus.published);
        LessonNote existing = note(3L, 1);
        when(notes.findById(new LessonNote.LessonNoteId(STUDENT, 3L))).thenReturn(Optional.of(existing));

        assertThat(service.saveNote(STUDENT, 3L, new LessonNoteDTO("   ", 1L))).isNull();
        verify(notes).delete(existing);
    }

    @Test
    void anteprimaETipoDellaLezione() {
        String longText = "parola ".repeat(80);
        assertThat(NotebookService.preview(List.of(page(Kind.text, longText)))).endsWith("…")
                .hasSizeLessThanOrEqualTo(NotebookService.PREVIEW_LENGTH + 1);
        assertThat(NotebookService.preview(List.of(page(Kind.video, null)))).isNull();
        assertThat(NotebookService.kindOf(List.of(page(Kind.video, null)))).isEqualTo("video");
        assertThat(NotebookService.kindOf(List.of(page(Kind.video, null), page(Kind.text, "x")))).isEqualTo("lesson");
    }
}
