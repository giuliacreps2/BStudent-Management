package giuliacrepaldi.BStudent_Management.services.student;

import giuliacrepaldi.BStudent_Management.entities.catalog.AcademySetting;
import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationOption;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationQuestion;
import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationTest;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.MissionRewardsDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeLockDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.CoursesPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionNodeDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.StudentTestResultDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.AcademySettingsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.CoursesRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionStepsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.MissionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.catalog.SubjectsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationOptionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationQuestionsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationRecommendationsRepository;
import giuliacrepaldi.BStudent_Management.repositories.orientation.OrientationTestsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.TopicsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SubjectSkillsRepository;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IDailyGoalsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IHintsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentGameStateService;
import giuliacrepaldi.BStudent_Management.services.orientation.interfaces.IOrientationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Letture dello studente: corsi, missioni, Palestra, test (blueprint §5.8, §5.9, §7.3, §8)
class StudentReadsTest {

    private static final long STUDENT = 7L;
    private static final long LATIN = 1L;

    private final Subject latin = subject(LATIN, "latino", "Latino", SubjectStatus.published);
    private final Subject greek = subject(2L, "greco", "Greco", SubjectStatus.published);
    private final Course course = Course.builder().courseId(5L).slug("primi-passi").courseTitle("Primi passi")
            .status(ContentStatus.published).accessTier(AccessTier.free).level(CourseLevel.beginner)
            .entryCourse(true).subject(latin).build();
    private final Skill grammar = Skill.builder().skillId(10L).codeSkill("grammar").skillName("Grammatica").build();
    private final Skill lexicon = Skill.builder().skillId(11L).codeSkill("lexicon").skillName("Lessico").build();

    private SubjectsRepository subjects;
    private CoursesRepository courses;
    private MissionsRepository missions;
    private MissionAttemptsRepository missionAttempts;
    private OrientationTestsRepository tests;
    private IEntitlementService entitlement;
    private IStudentGameStateService gameState;
    private ISkillLevelsService skillLevels;
    private PracticeExercisesRepository practiceExercises;
    private SubjectSkillsRepository subjectSkills;
    private StudentCatalogService catalogService;
    private StudentPracticeService practiceService;

    @BeforeEach
    void setUp() {
        subjects = mock(SubjectsRepository.class);
        courses = mock(CoursesRepository.class);
        missions = mock(MissionsRepository.class);
        missionAttempts = mock(MissionAttemptsRepository.class);
        tests = mock(OrientationTestsRepository.class);
        entitlement = mock(IEntitlementService.class);
        gameState = mock(IStudentGameStateService.class);
        skillLevels = mock(ISkillLevelsService.class);
        practiceExercises = mock(PracticeExercisesRepository.class);
        subjectSkills = mock(SubjectSkillsRepository.class);

        when(subjects.findByStatusOrderBySubjectId(SubjectStatus.published)).thenReturn(List.of(latin, greek));
        when(subjects.findBySlug("latino")).thenReturn(Optional.of(latin));
        when(courses.existsBySubject_SubjectIdAndStatus(LATIN, ContentStatus.published)).thenReturn(true);
        when(courses.findBySubject_SubjectIdAndStatusOrderByCourseId(LATIN, ContentStatus.published)).thenReturn(List.of(course));
        when(courses.findBySlug("primi-passi")).thenReturn(Optional.of(course));
        when(entitlement.canAccess(anyLong(), any(Course.class))).thenReturn(true);
        when(entitlement.canAccess(anyLong(), any(Mission.class))).thenReturn(true);
        when(gameState.getMissionRewards(anyLong())).thenReturn(new MissionRewardsDTO(new BigDecimal("50.00"), List.of()));
        when(subjectSkills.findBySubjectOrdered(LATIN)).thenReturn(List.of(
                SubjectSkill.builder().id(new SubjectSkill.SubjectSkillId(LATIN, 10L)).skill(grammar).sortOrder(1).build(),
                SubjectSkill.builder().id(new SubjectSkill.SubjectSkillId(LATIN, 11L)).skill(lexicon).sortOrder(2).build()));

        catalogService = new StudentCatalogService(subjects, courses, missions, missionAttempts,
                mock(SkillsRepository.class), entitlement, gameState, tests);
        practiceService = new StudentPracticeService(subjects, courses, subjectSkills, practiceExercises,
                mock(TopicsRepository.class), entitlement, skillLevels, mock(IDailyGoalsService.class));
    }

    private static Subject subject(long id, String slug, String name, SubjectStatus status) {
        Subject subject = new Subject();
        subject.setSubjectId(id);
        subject.setSlug(slug);
        subject.setSubjectName(name);
        subject.setTagline("Tagline di " + name);
        subject.setStatus(status);
        return subject;
    }

    private void missionsOfCourse(long... ids) {
        List<Mission> list = new ArrayList<>();
        for (long id : ids) {
            list.add(Mission.builder().missionId(id).slug("missione-" + id).missionTitle("Missione " + id)
                    .missionDescription("...").status(ContentStatus.published).course(course).build());
        }
        when(missions.findByCourse_CourseIdAndStatusOrderByMissionSortOrder(5L, ContentStatus.published)).thenReturn(list);
    }

    // ------------------------------------------------------------------ Corsi

    @Test
    void laMateriaSenzaCorsiEPrestoDisponibileENonDiventaAttiva() {
        missionsOfCourse(1, 2);
        CoursesPageDTO page = catalogService.getCoursesPage(STUDENT, "greco");

        assertThat(page.subjects().get(1).locked()).isTrue();
        assertThat(page.subjects().get(1).tagline()).isEqualTo("Presto disponibile");
        // Greco è chiuso: resta attiva la prima materia aperta
        assertThat(page.activeSubject().slug()).isEqualTo("latino");
        assertThat(page.courses()).hasSize(1);
        assertThat(page.courses().get(0).entryCourse()).isTrue();
        assertThat(page.courses().get(0).popular()).isFalse();
    }

    @Test
    void lAvanzamentoDelCorsoContaLeMissioniSuperate() {
        missionsOfCourse(1, 2, 3, 4);
        assertThat(catalogService.getCoursesPage(STUDENT, null).courses().get(0).progress()).isNull();

        when(missionAttempts.findPassedMissionIds(STUDENT, 5L)).thenReturn(List.of(1L));
        assertThat(catalogService.getCoursesPage(STUDENT, null).courses().get(0).progress()).isEqualTo(25);
    }

    @Test
    void ilCorsoAPagamentoHaIlLucchettoSenzaAccesso() {
        missionsOfCourse(1);
        when(entitlement.canAccess(STUDENT, course)).thenReturn(false);
        assertThat(catalogService.getCoursesPage(STUDENT, null).courses().get(0).premiumLocked()).isTrue();
    }

    @Test
    void unCorsoNonPubblicatoNonEsistePerLoStudente() {
        course.setStatus(ContentStatus.draft);
        assertThatThrownBy(() -> catalogService.getCourseDetail(STUDENT, "primi-passi")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void laPaginaDeiCorsiRiportaIlLivelloConsigliatoDalTest() {
        missionsOfCourse(1);
        OrientationTest test = new OrientationTest();
        test.setOrientationTestId(90L);
        when(tests.findBySubject_SubjectIdAndKind(LATIN, OrientationTestKind.course_level)).thenReturn(Optional.of(test));
        when(tests.findLastResult(STUDENT, 90L)).thenReturn(List.<Object[]>of(new Object[]{"intermediate", null, "2026-11-17T10:00:00Z"}));

        CoursesPageDTO page = catalogService.getCoursesPage(STUDENT, null);

        assertThat(page.hasLevelTest()).isTrue();
        assertThat(page.recommendedLevel()).isEqualTo(CourseLevel.intermediate);
    }

    // ------------------------------------------------------------------ Missioni

    private StudentMissionsService missionsService(int lives, Student student) {
        MissionStepsRepository steps = mock(MissionStepsRepository.class);
        when(steps.findByMissionOrdered(anyLong())).thenReturn(List.of());
        AcademySettingsRepository academy = mock(AcademySettingsRepository.class);
        when(academy.findAll()).thenReturn(List.of(AcademySetting.builder().passingThreshold(new BigDecimal("60.00")).build()));
        StudentsRepository students = mock(StudentsRepository.class);
        when(students.findById(STUDENT)).thenReturn(Optional.of(student));
        ILivesService livesService = mock(ILivesService.class);
        when(livesService.getLives(STUDENT)).thenReturn(new LivesDTO(lives, 5, null));
        return new StudentMissionsService(catalogService, steps, missionAttempts, academy, students, entitlement,
                gameState, livesService, mock(IHintsService.class));
    }

    @Test
    void loStatoDelleMissioniSiCalcolaDallOrdine() {
        missionsOfCourse(1, 2, 3);
        StudentMissionsService service = missionsService(5, new Student());
        // Nessuna superata: la prima è l'attuale
        assertThat(service.getMissionMap(STUDENT, "primi-passi").missions()).extracting(MissionNodeDTO::status)
                .containsExactly("current", "locked", "locked");

        when(missionAttempts.findPassedMissionIds(STUDENT, 5L)).thenReturn(List.of(1L));
        assertThat(service.getMissionMap(STUDENT, "primi-passi").missions()).extracting(MissionNodeDTO::status)
                .containsExactly("completed", "current", "locked");
    }

    @Test
    void ilLucchettoPremiumEDistintoDalloStato() {
        missionsOfCourse(1, 2);
        when(entitlement.canAccess(eq(STUDENT), any(Mission.class))).thenReturn(false);
        MissionNodeDTO first = missionsService(5, new Student()).getMissionMap(STUDENT, "primi-passi").missions().get(0);
        assertThat(first.status()).isEqualTo("current");
        assertThat(first.premiumLocked()).isTrue();
    }

    @Test
    void siParteSoloDallaMissioneAttualeEConAlmenoUnaVita() {
        missionsOfCourse(1, 2);
        MissionPageDTO current = missionsService(5, new Student()).getMissionPage(STUDENT, "primi-passi", "missione-1");
        assertThat(current.canStart()).isTrue();
        assertThat(current.passingThreshold()).isEqualByComparingTo("60");
        assertThat(current.mission().xp()).isEqualByComparingTo("50");

        assertThat(missionsService(5, new Student()).getMissionPage(STUDENT, "primi-passi", "missione-2").canStart()).isFalse();
        assertThat(missionsService(0, new Student()).getMissionPage(STUDENT, "primi-passi", "missione-1").canStart()).isFalse();
    }

    @Test
    void ilTutorialSiSegnaUnaVoltaSola() {
        Student student = new Student();
        StudentMissionsService service = missionsService(5, student);
        assertThat(service.isTutorialSeen(STUDENT)).isFalse();

        service.markTutorialSeen(STUDENT);
        OffsetDateTime first = student.getTutorialSeenAt();
        service.markTutorialSeen(STUDENT);

        assertThat(service.isTutorialSeen(STUDENT)).isTrue();
        assertThat(student.getTutorialSeenAt()).isEqualTo(first);
    }

    // ------------------------------------------------------------------ Palestra

    private PracticeExercise practice(long id, Skill skill, int difficulty, AccessTier tier) {
        QuestionBankEntry entry = new QuestionBankEntry();
        entry.setTitle("Esercizio " + id);
        entry.setStatus(ContentStatus.published);
        return PracticeExercise.builder().practiceExerciseId(id).skill(skill).difficulty(difficulty).accessTier(tier)
                .status(ContentStatus.published).maxPoints(new BigDecimal("10.00")).questionBankEntry(entry)
                .topic(Topic.builder().topicId(80L).topicName("Prima declinazione").subject(latin).build()).build();
    }

    // Righe dello stato personale: voce, percentuale migliore, superata, bozza, in correzione, ultima attività
    private void practiceOfLatin(List<PracticeExercise> exercises, Object[]... states) {
        when(practiceExercises.findVisibleBySubject(LATIN)).thenReturn(exercises);
        when(practiceExercises.findPersonalStates(STUDENT)).thenReturn(List.of(states));
        when(skillLevels.getLocks(eq(STUDENT), anyCollection())).thenAnswer(invocation -> {
            Map<Long, PracticeLockDTO> locks = new HashMap<>();
            for (PracticeExercise exercise : exercises) {
                boolean locked = exercise.getDifficulty() > 2;
                locks.put(exercise.getPracticeExerciseId(), new PracticeLockDTO(exercise.getPracticeExerciseId(),
                        exercise.getDifficulty(), locked, locked ? exercise.getDifficulty() - 1 : null, null));
            }
            return locks;
        });
        when(skillLevels.getLevels(STUDENT, LATIN)).thenReturn(List.of(
                new SkillLevelStateDTO(10L, "grammar", "Grammatica", null, null, new BigDecimal("30"), 2, false, 33, new BigDecimal("20")),
                new SkillLevelStateDTO(11L, "lexicon", "Lessico", null, null, new BigDecimal("5"), 1, false, 25, new BigDecimal("15"))));
    }

    @Test
    void loStatoPersonaleDiUnaVoceVieneDaiTentativi() {
        practiceOfLatin(List.of(practice(1, grammar, 1, AccessTier.free), practice(2, grammar, 1, AccessTier.free),
                        practice(3, grammar, 1, AccessTier.free), practice(4, grammar, 1, AccessTier.free),
                        practice(5, grammar, 1, AccessTier.premium)),
                new Object[]{1L, new BigDecimal("87.00"), true, false, false, new BigDecimal("1700000000")},
                new Object[]{2L, new BigDecimal("40.00"), false, false, false, new BigDecimal("1700000100")},
                new Object[]{3L, null, false, true, false, new BigDecimal("1700000200")},
                new Object[]{4L, null, false, false, true, new BigDecimal("1700000300")});

        List<PracticeItemDTO> items = practiceService.getPracticeBySkill(STUDENT, "latino", "grammar").topics().get(0).items();

        assertThat(items).extracting(PracticeItemDTO::status).containsExactly("completed", "retry", "saved", "in_review", "new");
        // 87% di 10 punti; sotto il 60% nessun punto (§10.1)
        assertThat(items.get(0).points()).isEqualByComparingTo("8.70");
        assertThat(items.get(1).points()).isEqualByComparingTo("0");
        // Senza abbonamento la voce premium ha il lucchetto premium, distinto da quello della difficoltà
        assertThat(items.get(4).premiumLocked()).isTrue();
        assertThat(items.get(4).lock().locked()).isFalse();
    }

    @Test
    void lObiettivoDiOggiProponePrimaLeVociDaRifarePoiLeNuoveEMaiQuelleBloccate() {
        practiceOfLatin(List.of(practice(1, grammar, 1, AccessTier.free), practice(2, grammar, 1, AccessTier.free),
                        practice(3, grammar, 2, AccessTier.free), practice(4, grammar, 3, AccessTier.free),
                        practice(5, grammar, 1, AccessTier.premium), practice(6, grammar, 1, AccessTier.free)),
                new Object[]{1L, new BigDecimal("90.00"), true, false, false, new BigDecimal("1700000000")},
                new Object[]{2L, new BigDecimal("30.00"), false, false, false, new BigDecimal("1700000100")});

        List<PracticeItemDTO> goal = practiceService.getTodayGoal(STUDENT, "latino", "grammar");

        // 2 da rifare, poi le nuove dalla più facile (6, 3); la 4 è bloccata per difficoltà, la 5 è premium
        assertThat(goal).extracting(PracticeItemDTO::practiceExerciseId).containsExactly(2L, 6L, 3L);
    }

    @Test
    void sorprendimiSceglieLaSkillPiuDebole() {
        practiceOfLatin(List.of(practice(1, grammar, 1, AccessTier.free), practice(2, lexicon, 1, AccessTier.free)));
        assertThat(practiceService.getTodayGoal(STUDENT, "latino", null))
                .extracting(PracticeItemDTO::skillCode).containsExactly("lexicon");
    }

    // ------------------------------------------------------------------ Test di livello

    private StudentTestsService testsService(List<CourseLevel> optionLevels) {
        OrientationTest test = new OrientationTest();
        test.setOrientationTestId(90L);
        test.setKind(OrientationTestKind.course_level);
        when(tests.findBySubject_SubjectIdAndKind(LATIN, OrientationTestKind.course_level)).thenReturn(Optional.of(test));
        List<OrientationQuestion> questions = new ArrayList<>();
        List<OrientationOption> options = new ArrayList<>();
        for (int i = 0; i < optionLevels.size(); i++) {
            OrientationQuestion question = new OrientationQuestion();
            question.setOrientationQuestionId(100L + i);
            questions.add(question);
            // Ogni domanda ha un'opzione con il livello indicato: id opzione = 200 + posizione
            OrientationOption option = new OrientationOption();
            option.setOrientationOptionId(200L + i);
            option.setOrientationQuestion(question);
            option.setLevel(optionLevels.get(i));
            options.add(option);
        }
        OrientationQuestionsRepository questionsRepository = mock(OrientationQuestionsRepository.class);
        when(questionsRepository.findByOrientationTest_OrientationTestIdOrderByQuestionSortOrder(90L)).thenReturn(questions);
        OrientationOptionsRepository optionsRepository = mock(OrientationOptionsRepository.class);
        when(optionsRepository.findByQuestionIds(anyCollection())).thenReturn(options);
        IOrientationService orientation = mock(IOrientationService.class);
        when(orientation.checkReadiness(90L)).thenReturn(List.of());
        when(skillLevels.getMaxLevel(anyLong())).thenReturn(6);
        return new StudentTestsService(subjects, tests, questionsRepository, optionsRepository,
                mock(OrientationRecommendationsRepository.class), subjectSkills, orientation, practiceService, skillLevels);
    }

    @Test
    void ilTestDiLivelloSalvaIlRisultatoEAssegnaIlLivelloDiPartenzaATutteLeSkill() {
        StudentTestsService service = testsService(List.of(CourseLevel.advanced, CourseLevel.intermediate, CourseLevel.intermediate));

        StudentTestResultDTO result = service.submit(STUDENT, "latino", OrientationTestKind.course_level,
                Map.of(100L, 200L, 101L, 201L, 102L, 202L));

        // Intermedio scelto due volte: livello Intermedio, partenza al livello 2 in Grammatica e Lessico
        assertThat(result.level()).isEqualTo(CourseLevel.intermediate);
        verify(tests).saveLevelResult(eq(STUDENT), eq(90L), anyString(), eq("intermediate"));
        verify(skillLevels).setStartingLevel(STUDENT, LATIN, 10L, 2);
        verify(skillLevels).setStartingLevel(STUDENT, LATIN, 11L, 2);
    }

    @Test
    void ilTestRichiedeTutteLeRisposteEOpzioniDellaLoroDomanda() {
        StudentTestsService service = testsService(List.of(CourseLevel.beginner, CourseLevel.beginner, CourseLevel.advanced));

        assertThatThrownBy(() -> service.submit(STUDENT, "latino", OrientationTestKind.course_level, Map.of(100L, 200L)))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("tutte le domande");
        // L'opzione 201 è della domanda 101, non della 100
        assertThatThrownBy(() -> service.submit(STUDENT, "latino", OrientationTestKind.course_level,
                Map.of(100L, 201L, 101L, 201L, 102L, 202L)))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("non appartiene");
        verify(tests, never()).saveLevelResult(anyLong(), anyLong(), anyString(), anyString());
        verify(skillLevels, never()).setStartingLevel(anyLong(), anyLong(), anyLong(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void unTestNonProntoNonSiVede() {
        StudentTestsService service = testsService(List.of(CourseLevel.beginner));
        // Nessun test di orientamento per la materia
        assertThat(service.getTest("latino", OrientationTestKind.exercises)).isNull();
        assertThat(service.getTest("latino", OrientationTestKind.course_level).questions()).hasSize(1);
    }
}
