package giuliacrepaldi.BStudent_Management.services.progression;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionCheckpoint;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRun;
import giuliacrepaldi.BStudent_Management.entities.catalog.AcademySetting;
import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;
import giuliacrepaldi.BStudent_Management.entities.progression.CourseResult;
import giuliacrepaldi.BStudent_Management.entities.progression.MissionRecord;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.progression.SourceKind;
import giuliacrepaldi.BStudent_Management.payloads.progression.CourseResultDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.MissionPassedDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.SkillPointsDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionCheckpointsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.AcademySettingsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExerciseSkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.CourseResultsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.MissionRecordsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.MissionSkillContributionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.progression.SkillCreditEntriesRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.ICourseProgressService;
import giuliacrepaldi.BStudent_Management.services.progression.interfaces.ISkillCreditsService;
import giuliacrepaldi.BStudent_Management.utilities.AttemptJson;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Punti skill, record della missione e risultato del corso (blueprint §10.3, §10.4; Blueprint Svolgimento §5)
class MissionProgressTest {

    private static final long STUDENT = 7L;
    private static final long ATTEMPT = 1000L;
    private static final long MISSION_ATTEMPT = 1L;
    private static final long TRANSLATION = 11L;
    private static final long VOCABULARY = 12L;

    private ExerciseAssessmentsRepository assessments;
    private MissionRecordsRepository records;
    private ISkillCreditsService credits;
    private IStudentXpService xp;
    private MissionProgressService service;
    private final List<Object> persisted = new ArrayList<>();

    private Mission mission;
    private MissionAttempt missionAttempt;
    private MissionRun run;

    @BeforeEach
    void setUp() {
        Subject latin = new Subject();
        latin.setSubjectId(100L);
        Course course = new Course();
        course.setCourseId(200L);
        course.setSubject(latin);
        mission = new Mission();
        mission.setMissionId(300L);
        mission.setCourse(course);
        run = MissionRun.builder().missionRunId(1L).mission(mission).build();
        missionAttempt = MissionAttempt.builder().missionAttemptId(MISSION_ATTEMPT).missionRun(run).build();

        assessments = mock(ExerciseAssessmentsRepository.class);
        records = mock(MissionRecordsRepository.class);
        credits = mock(ISkillCreditsService.class);
        xp = mock(IStudentXpService.class);
        MissionExerciseSkillsRepository skills = mock(MissionExerciseSkillsRepository.class);
        when(skills.findWithSkill(10L)).thenReturn(List.of(share(TRANSLATION, "70"), share(VOCABULARY, "30")));
        AcademySettingsRepository academy = mock(AcademySettingsRepository.class);
        when(academy.findById((short) 1)).thenReturn(Optional.of(AcademySetting.builder()
                .missionSkillFactor(new BigDecimal("0.1000")).passingThreshold(new BigDecimal("60.00")).build()));
        SkillCreditEntriesRepository entries = mock(SkillCreditEntriesRepository.class);
        when(entries.sumBestMissionContributions(eq(STUDENT), anyLong(), anyLong())).thenReturn(new BigDecimal("0.56"));
        MissionAttemptsRepository missionAttempts = mock(MissionAttemptsRepository.class);
        when(missionAttempts.findById(MISSION_ATTEMPT)).thenReturn(Optional.of(missionAttempt));
        MissionCheckpointsRepository checkpoints = mock(MissionCheckpointsRepository.class);
        // Esercizio da 10 punti al 100% ed esercizio da 20 punti al 70%: risultato 80%
        when(checkpoints.findWithAssessment(1L)).thenReturn(List.of(checkpoint("10.00", "100.00"), checkpoint("20.00", "70.00")));
        ICourseProgressService courses = mock(ICourseProgressService.class);
        when(courses.onMissionPassed(STUDENT, 300L)).thenReturn(new CourseResultDTO(200L, "Latino 1", 1, 3, null, false, List.of()));
        EntityManager entityManager = mock(EntityManager.class);
        doAnswer(invocation -> persisted.add(invocation.getArgument(0))).when(entityManager).persist(any());

        service = new MissionProgressService(assessments, missionAttempts, checkpoints, skills,
                mock(MissionSkillContributionsRepository.class), records, entries, academy, mock(StudentsRepository.class),
                credits, xp, courses, entityManager);
    }

    private static MissionExerciseSkill share(long skillId, String weight) {
        return MissionExerciseSkill.builder().skill(Skill.builder().skillId(skillId).build()).weight(new BigDecimal(weight)).build();
    }

    private static MissionCheckpoint checkpoint(String maxPoints, String percentage) {
        ExerciseAttempt attempt = ExerciseAttempt.builder()
                .configurationSnapshot(AttemptJson.writeConfiguration("missionExerciseId", 10L, new BigDecimal(maxPoints), List.of()))
                .build();
        return MissionCheckpoint.builder().exerciseAssessment(ExerciseAssessment.builder().exerciseAttempt(attempt)
                .percentage(new BigDecimal(percentage)).build()).build();
    }

    private void assessed(String percentage) {
        MissionExercise exercise = MissionExercise.builder().missionExerciseId(10L).mission(mission)
                .maxPoints(new BigDecimal("10.00")).status(ContentStatus.published).build();
        ExerciseAttempt attempt = ExerciseAttempt.builder().exerciseAttemptId(ATTEMPT).context(ExerciseContext.mission)
                .missionExercise(exercise).missionAttempt(missionAttempt)
                .configurationSnapshot(AttemptJson.writeConfiguration("missionExerciseId", 10L, new BigDecimal("10.00"), List.of()))
                .build();
        when(assessments.findById(ATTEMPT)).thenReturn(Optional.of(ExerciseAssessment.builder().exerciseAttempt(attempt)
                .percentage(new BigDecimal(percentage)).passingThreshold(new BigDecimal("60.00")).build()));
    }

    @Test
    void iPuntiSkillSiDividonoSecondoLeQuoteDellEsercizio() {
        assessed("80.00");
        when(credits.creditImprovement(eq(STUDENT), eq(100L), eq(TRANSLATION), eq(SourceKind.mission), eq(10L), any(), any()))
                .thenReturn(new BigDecimal("0.56"));
        when(credits.creditImprovement(eq(STUDENT), eq(100L), eq(VOCABULARY), eq(SourceKind.mission), eq(10L), any(), any()))
                .thenReturn(new BigDecimal("0.24"));

        SkillPointsDTO points = service.onExerciseAssessed(STUDENT, ATTEMPT);

        // 10 punti, 80%, fattore 10%: 0,56 a Traduzione e 0,24 a Lessico, nella materia della missione
        verify(credits).creditImprovement(STUDENT, 100L, TRANSLATION, SourceKind.mission, 10L, new BigDecimal("0.56"), "mission:" + ATTEMPT);
        verify(credits).creditImprovement(STUDENT, 100L, VOCABULARY, SourceKind.mission, 10L, new BigDecimal("0.24"), "mission:" + ATTEMPT);
        assertThat(points.earned()).isEqualByComparingTo("0.80");
        verify(xp).onSkillPointsCredited(STUDENT);
    }

    @Test
    void senzaMiglioramentoNessunAccreditoENessunaEvoluzione() {
        assessed("40.00");
        when(credits.creditImprovement(anyLong(), anyLong(), anyLong(), any(), anyLong(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        SkillPointsDTO points = service.onExerciseAssessed(STUDENT, ATTEMPT);

        assertThat(points.earned()).isEqualByComparingTo("0");
        verify(xp, never()).onSkillPointsCredited(STUDENT);
    }

    @Test
    void ilRisultatoDellaMissionePesaGliEserciziEDiventaIlRecord() {
        when(xp.getTotalXp(STUDENT)).thenReturn(new BigDecimal("100"), new BigDecimal("130"));
        when(xp.onMissionPassed(STUDENT, 300L, MISSION_ATTEMPT)).thenReturn(List.of(2));

        MissionPassedDTO passed = service.onMissionPassed(STUDENT, MISSION_ATTEMPT);

        assertThat(passed.percentage()).isEqualByComparingTo("80");
        assertThat(passed.newRecord()).isTrue();
        assertThat(passed.xpEarned()).isEqualByComparingTo("30");
        assertThat(passed.newStages()).containsExactly(2);
        assertThat(persisted).hasSize(1).first().isInstanceOf(MissionRecord.class);
    }

    @Test
    void unaRipetizionePeggioreNonAbbassaIlRecordENonDaXp() {
        MissionRecord record = MissionRecord.builder().percentage(new BigDecimal("95.00")).build();
        when(records.findById(any())).thenReturn(Optional.of(record));
        when(xp.getTotalXp(STUDENT)).thenReturn(new BigDecimal("130"));

        MissionPassedDTO passed = service.onMissionPassed(STUDENT, MISSION_ATTEMPT);

        assertThat(passed.newRecord()).isFalse();
        assertThat(record.getPercentage()).isEqualByComparingTo("95");
        assertThat(passed.xpEarned()).isEqualByComparingTo("0");
    }

    // ------------------------------------------------------------------ Corso

    @Test
    void ilCorsoSiCompletaConTutteLeMissioniVisibiliEUnaVoltaSola() {
        MissionsRepository missions = mock(MissionsRepository.class);
        when(missions.findById(300L)).thenReturn(Optional.of(mission));
        when(missions.findByCourse_CourseIdAndStatusOrderByMissionSortOrder(200L, ContentStatus.published))
                .thenReturn(List.of(missionWithId(300L), missionWithId(301L)));
        MissionAttemptsRepository attempts = mock(MissionAttemptsRepository.class);
        when(attempts.findPassedMissionIds(STUDENT, 200L)).thenReturn(List.of(300L, 301L));
        MissionRecordsRepository courseRecords = mock(MissionRecordsRepository.class);
        when(courseRecords.findPercentages(eq(STUDENT), any())).thenReturn(List.of(new BigDecimal("80"), new BigDecimal("100")));
        CourseResultsRepository results = mock(CourseResultsRepository.class);
        when(results.findById(any())).thenReturn(Optional.empty());
        EntityManager entityManager = mock(EntityManager.class);
        List<CourseResult> saved = new ArrayList<>();
        doAnswer(invocation -> saved.add(invocation.getArgument(0))).when(entityManager).persist(any());
        when(xp.onCourseCompleted(STUDENT, 200L)).thenReturn(List.of(1));
        CourseProgressService courseService = new CourseProgressService(missions, attempts, courseRecords, results,
                mock(StudentsRepository.class), xp, entityManager);

        CourseResultDTO first = courseService.onMissionPassed(STUDENT, 300L);

        assertThat(first.completedNow()).isTrue();
        assertThat(first.percentage()).isEqualByComparingTo("90");
        assertThat(first.newStages()).containsExactly(1);
        assertThat(saved.getFirst().getCompletedAt()).isNotNull();

        // Una ripetizione dopo: il corso è già completato, nessun nuovo premio
        when(results.findById(any())).thenReturn(Optional.of(saved.getFirst()));
        CourseResultDTO again = courseService.onMissionPassed(STUDENT, 300L);

        assertThat(again.completedNow()).isFalse();
        verify(xp).onCourseCompleted(STUDENT, 200L);
    }

    private Mission missionWithId(long id) {
        Mission other = new Mission();
        other.setMissionId(id);
        other.setCourse(mission.getCourse());
        return other;
    }
}
