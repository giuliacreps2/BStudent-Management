package giuliacrepaldi.BStudent_Management.services.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonPagesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionStepsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExerciseSkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionVersionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.QuestionsRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ICoursesService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Un esercizio corretto dalla docente può essere solo l'ultimo esercizio della missione (Blueprint Svolgimento §1)
class TeacherExerciseLastTest {

    private static final long MISSION = 300L;
    private static final long TEACHER_ENTRY = 41L;

    private final List<MissionStep> steps = new ArrayList<>();
    private MissionsService service;
    private Mission mission;

    @BeforeEach
    void setUp() {
        mission = new Mission();
        mission.setMissionId(MISSION);
        MissionStepsRepository stepsRepository = mock(MissionStepsRepository.class);
        when(stepsRepository.findByMissionOrdered(MISSION)).thenAnswer(invocation -> List.copyOf(steps));
        QuestionsRepository questions = mock(QuestionsRepository.class);
        when(questions.isTeacherAssessed(TEACHER_ENTRY)).thenReturn(true);
        service = new MissionsService(mock(MissionsRepository.class), stepsRepository, mock(LessonPagesRepository.class),
                mock(MissionExerciseSkillsRepository.class), mock(QuestionVersionsRepository.class), questions,
                mock(ICoursesService.class), mock(EntityManager.class));
    }

    private MissionStep lesson(int order) {
        Lesson lesson = new Lesson();
        lesson.setLessonId((long) order);
        lesson.setMission(mission);
        lesson.setStatus(ContentStatus.published);
        return MissionStep.of(lesson, order);
    }

    private MissionStep exercise(long entryId, int order, ContentStatus entryStatus) {
        QuestionBankEntry entry = new QuestionBankEntry();
        entry.setQuestionBankEntryId(entryId);
        entry.setInternalQuestionTitle("Traduzione " + entryId);
        entry.setStatus(entryStatus);
        return MissionStep.of(MissionExercise.builder().missionExerciseId(entryId).mission(mission)
                .questionBankEntry(entry).build(), order);
    }

    @Test
    void inFondoVaBeneAncheConUnaLezioneDopo() {
        steps.addAll(List.of(lesson(1), exercise(40L, 2, ContentStatus.published), lesson(3),
                exercise(TEACHER_ENTRY, 4, ContentStatus.published), lesson(5)));

        assertThatCode(() -> service.assertTeacherExerciseLast(MISSION)).doesNotThrowAnyException();
    }

    @Test
    void unAltroEsercizioDopoDiLuiVieneRifiutatoSubitoConCosaFare() {
        steps.addAll(List.of(lesson(1), exercise(TEACHER_ENTRY, 2, ContentStatus.published), lesson(3),
                exercise(40L, 4, ContentStatus.published)));

        assertThatThrownBy(() -> service.assertTeacherExerciseLast(MISSION))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Traduzione 41").hasMessageContaining("spostalo in fondo");
    }

    @Test
    void gliEserciziRitiratiDopoDiLuiNonContano() {
        steps.addAll(List.of(lesson(1), exercise(TEACHER_ENTRY, 2, ContentStatus.published), lesson(3),
                exercise(40L, 4, ContentStatus.withdrawn)));

        assertThatCode(() -> service.assertTeacherExerciseLast(MISSION)).doesNotThrowAnyException();
    }
}
