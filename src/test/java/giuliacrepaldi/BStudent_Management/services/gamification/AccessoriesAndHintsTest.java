package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRun;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.gamification.Accessory;
import giuliacrepaldi.BStudent_Management.entities.gamification.AccessoryBenefit;
import giuliacrepaldi.BStudent_Management.entities.gamification.AccessoryChargeState;
import giuliacrepaldi.BStudent_Management.entities.gamification.EquippedAccessory;
import giuliacrepaldi.BStudent_Management.entities.gamification.HintUse;
import giuliacrepaldi.BStudent_Management.entities.gamification.StudentAccessory;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.gamification.AccessoryStatus;
import giuliacrepaldi.BStudent_Management.exceptions.BadRequestException;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.HintDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StudentAccessoryDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.exercises.MissionExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.AccessoriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.AccessoryBenefitsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.AccessoryChargeStatesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.EquippedAccessoriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.HintUsesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.StudentAccessoriesRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.repositories.skills.SkillsRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Cappello da turista: 50 punti di Grammatica, 5 aiuti, ricarica di 12 ore
// (blueprint principale §11; Blueprint Gamification §6.1, §6.2)
class AccessoriesAndHintsTest {

    private static final long STUDENT = 7L;
    private static final long HAT = 3L;
    private static final long GRAMMAR = 10L;
    private static final long ATTEMPT = 40L;
    private static final long EXERCISE = 60L;
    private static final long TOPIC = 80L;

    private StudentAccessoriesRepository studentAccessories;
    private EquippedAccessoriesRepository equippedAccessories;
    private AccessoryBenefitsRepository benefits;
    private AccessoryChargeStatesRepository chargeStates;
    private MissionAttemptsRepository attempts;
    private MissionExercisesRepository missionExercises;
    private HintUsesRepository hintUses;
    private ResourcesRepository resources;
    private ISkillLevelsService skillLevels;
    private EntityManager entityManager;
    private AccessoriesService accessoriesService;
    private HintsService hintsService;

    private final Accessory hat = Accessory.builder().accessoryId(HAT).accessoryCode("cappello-da-turista")
            .accessoryName("Cappello da turista").accessorySlotCode("testa").requiredPoints(new BigDecimal("50.00"))
            .isAvailable(true).skill(Skill.builder().skillId(GRAMMAR).skillName("Grammatica").build()).build();
    private final AccessoryBenefit benefit = AccessoryBenefit.builder().accessoryId(HAT).accessory(hat)
            .kind(AccessoriesService.TOPIC_HINT).capacity(5).rechargeHours(12).build();

    @BeforeEach
    void setUp() {
        AccessoriesRepository accessories = mock(AccessoriesRepository.class);
        studentAccessories = mock(StudentAccessoriesRepository.class);
        equippedAccessories = mock(EquippedAccessoriesRepository.class);
        benefits = mock(AccessoryBenefitsRepository.class);
        chargeStates = mock(AccessoryChargeStatesRepository.class);
        attempts = mock(MissionAttemptsRepository.class);
        missionExercises = mock(MissionExercisesRepository.class);
        hintUses = mock(HintUsesRepository.class);
        resources = mock(ResourcesRepository.class);
        skillLevels = mock(ISkillLevelsService.class);
        entityManager = mock(EntityManager.class);
        StudentsRepository students = mock(StudentsRepository.class);
        when(students.getReferenceById(STUDENT)).thenReturn(new Student());

        when(accessories.findById(HAT)).thenReturn(Optional.of(hat));
        when(accessories.findAllByOrderByAccessoryId()).thenReturn(List.of(hat));
        when(benefits.findById(HAT)).thenReturn(Optional.of(benefit));
        when(benefits.existsById(HAT)).thenReturn(true);
        when(studentAccessories.findById_StudentId(STUDENT)).thenReturn(List.of());
        when(equippedAccessories.findById_StudentId(STUDENT)).thenReturn(List.of());

        accessoriesService = new AccessoriesService(accessories, benefits, studentAccessories, equippedAccessories,
                chargeStates, attempts, mock(SkillsRepository.class), students, skillLevels, entityManager);
        hintsService = new HintsService(studentAccessories, equippedAccessories, benefits, chargeStates, hintUses,
                attempts, missionExercises, resources, students, accessoriesService, entityManager);
    }

    private void grammarPoints(String points) {
        when(skillLevels.getBestPoints(STUDENT, GRAMMAR)).thenReturn(new BigDecimal(points));
    }

    private void owned() {
        when(studentAccessories.existsById(new StudentAccessory.StudentAccessoryId(STUDENT, HAT))).thenReturn(true);
        when(studentAccessories.findById_StudentId(STUDENT)).thenReturn(List.of(StudentAccessory.builder()
                .id(new StudentAccessory.StudentAccessoryId(STUDENT, HAT)).accessory(hat).build()));
    }

    private void equipped() {
        owned();
        when(equippedAccessories.findById_StudentId(STUDENT)).thenReturn(List.of(EquippedAccessory.builder()
                .id(new EquippedAccessory.EquippedAccessoryId(STUDENT, "testa")).accessory(hat).build()));
    }

    private AccessoryChargeState charges(int remaining, OffsetDateTime rechargeAt) {
        AccessoryChargeState state = AccessoryChargeState.builder()
                .id(new AccessoryChargeState.AccessoryChargeStateId(STUDENT, HAT))
                .remainingCharges(remaining).rechargeAt(rechargeAt).build();
        when(chargeStates.findById(new AccessoryChargeState.AccessoryChargeStateId(STUDENT, HAT)))
                .thenReturn(Optional.of(state));
        return state;
    }

    // Esercizio di una missione in corso, con o senza argomento
    private void missionExercise(MissionAttemptStatus status, boolean withTopic) {
        Mission mission = new Mission();
        mission.setMissionId(1L);
        Student student = new Student();
        student.setStudentId(STUDENT);
        MissionRun run = new MissionRun();
        run.setMission(mission);
        MissionAttempt attempt = new MissionAttempt();
        attempt.setMissionAttemptId(ATTEMPT);
        attempt.setStudent(student);
        attempt.setMissionRun(run);
        attempt.setStatus(status);
        when(attempts.findById(ATTEMPT)).thenReturn(Optional.of(attempt));

        Subject latin = new Subject();
        latin.setSlug("latino");
        QuestionBankEntry entry = new QuestionBankEntry();
        if (withTopic) {
            entry.setTopic(Topic.builder().topicId(TOPIC).topicName("Terza declinazione").subject(latin).build());
        }
        MissionExercise exercise = new MissionExercise();
        exercise.setMissionExerciseId(EXERCISE);
        exercise.setMission(mission);
        exercise.setQuestionBankEntry(entry);
        when(missionExercises.findById(EXERCISE)).thenReturn(Optional.of(exercise));
    }

    // ------------------------------------------------------------------ Accessori

    @Test
    void sottoSogliaIlCappelloEBloccatoEDiceQuantoServe() {
        grammarPoints("30");
        StudentAccessoryDTO dto = accessoriesService.getAccessories(STUDENT).get(0);
        assertThat(dto.status()).isEqualTo(AccessoryStatus.locked);
        assertThat(dto.unlockHint()).isEqualTo("Si sblocca con 50 punti di Grammatica");
        assertThat(dto.benefitDescription()).isEqualTo("Ti ricorda l'argomento · 5 aiuti · si ricarica in 12 ore");
        assertThat(dto.charges()).isNull();
    }

    @Test
    void riscattareSottoSogliaDiceIPuntiMancanti() {
        grammarPoints("42.5");
        assertThatThrownBy(() -> accessoriesService.redeem(STUDENT, HAT))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Ti mancano 7,5 punti di Grammatica per riscattare Cappello da turista");
        verify(entityManager, never()).persist(any());
    }

    @Test
    void aSogliaSiRiscattaConLeCaricheCompleteENonSiIndossaDaSolo() {
        grammarPoints("50");
        StudentAccessoryDTO dto = accessoriesService.redeem(STUDENT, HAT);
        // Posseduto, non indossato; punti non consumati
        assertThat(dto.status()).isEqualTo(AccessoryStatus.owned);
        assertThat(dto.currentPoints()).isEqualByComparingTo("50");
        verify(entityManager).persist(any(StudentAccessory.class));
        verify(entityManager).persist(any(AccessoryChargeState.class));
        verify(entityManager, never()).persist(any(EquippedAccessory.class));
    }

    @Test
    void ilDoppioClicSuRiscattaNonCreaDuePossessi() {
        grammarPoints("50");
        owned();
        charges(5, null);
        assertThat(accessoriesService.redeem(STUDENT, HAT).status()).isEqualTo(AccessoryStatus.owned);
        verify(entityManager, never()).persist(any());
    }

    @Test
    void nonSiIndossaSenzaAverloRiscattatoNeDuranteUnaMissione() {
        grammarPoints("50");
        assertThatThrownBy(() -> accessoriesService.equip(STUDENT, HAT))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("Riscatta");

        owned();
        when(attempts.existsByStudent_StudentIdAndStatusIn(eq(STUDENT), anyCollection())).thenReturn(true);
        assertThatThrownBy(() -> accessoriesService.equip(STUDENT, HAT))
                .isInstanceOf(ConflictException.class).hasMessageContaining("missione in corso");
    }

    // ------------------------------------------------------------------ Aiuto del cappello

    @Test
    void lAiutoDiceLArgomentoELInBreveEConsumaUnaCarica() {
        equipped();
        AccessoryChargeState state = charges(5, null);
        missionExercise(MissionAttemptStatus.active, true);
        when(resources.findFirstByTopic_TopicIdAndStatusOrderByResourceId(TOPIC, ContentStatus.published))
                .thenReturn(Optional.of(Resource.builder().slug("terza-declinazione").title("La terza declinazione")
                        .summaryText("Temi in consonante e in -i.")
                        .content("{\"summaryExample\":{\"term\":\"rex, regis\",\"translation\":\"il re\"}}").build()));

        HintDTO hint = hintsService.useHint(STUDENT, ATTEMPT, EXERCISE);

        assertThat(hint.topicName()).isEqualTo("Terza declinazione");
        assertThat(hint.summaryText()).isEqualTo("Temi in consonante e in -i.");
        assertThat(hint.summaryExample().get("term").asString()).isEqualTo("rex, regis");
        assertThat(hint.alreadyOpened()).isFalse();
        assertThat(hint.charges().remaining()).isEqualTo(4);
        assertThat(state.getRemainingCharges()).isEqualTo(4);
        verify(entityManager).persist(any(HintUse.class));
    }

    @Test
    void riaprireLoStessoAiutoNelloStessoTentativoEGratis() {
        equipped();
        AccessoryChargeState state = charges(4, null);
        missionExercise(MissionAttemptStatus.active, true);
        when(hintUses.existsById(new HintUse.HintUseId(STUDENT, ATTEMPT, EXERCISE))).thenReturn(true);

        HintDTO hint = hintsService.useHint(STUDENT, ATTEMPT, EXERCISE);

        assertThat(hint.alreadyOpened()).isTrue();
        // Argomento senza Risorsa pubblicata: il cappello dice solo l'argomento
        assertThat(hint.summaryText()).isNull();
        assertThat(state.getRemainingCharges()).isEqualTo(4);
        verify(entityManager, never()).persist(any());
    }

    @Test
    void lUltimaCaricaAvviaLaRicaricaDiDodiciOre() {
        equipped();
        AccessoryChargeState state = charges(1, null);
        missionExercise(MissionAttemptStatus.active, true);

        HintDTO hint = hintsService.useHint(STUDENT, ATTEMPT, EXERCISE);

        assertThat(hint.charges().remaining()).isZero();
        assertThat(state.getRechargeAt()).isAfter(OffsetDateTime.now().plusHours(11));
    }

    @Test
    void senzaCaricheLAiutoDiceQuandoTornano() {
        equipped();
        charges(0, OffsetDateTime.now().plusHours(3));
        missionExercise(MissionAttemptStatus.active, true);
        assertThatThrownBy(() -> hintsService.useHint(STUDENT, ATTEMPT, EXERCISE))
                .isInstanceOf(ConflictException.class).hasMessageContaining("Hai finito gli aiuti");
    }

    @Test
    void unEsercizioSenzaArgomentoNonConsumaCariche() {
        equipped();
        AccessoryChargeState state = charges(5, null);
        missionExercise(MissionAttemptStatus.active, false);
        assertThatThrownBy(() -> hintsService.useHint(STUDENT, ATTEMPT, EXERCISE))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("non ha suggerimenti");
        assertThat(state.getRemainingCharges()).isEqualTo(5);
    }

    @Test
    void lAiutoRichiedeIlCappelloIndossatoEUnaMissioneInCorso() {
        owned();
        charges(5, null);
        missionExercise(MissionAttemptStatus.active, true);
        assertThatThrownBy(() -> hintsService.useHint(STUDENT, ATTEMPT, EXERCISE))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("Indossa");

        equipped();
        missionExercise(MissionAttemptStatus.passed, true);
        assertThatThrownBy(() -> hintsService.useHint(STUDENT, ATTEMPT, EXERCISE))
                .isInstanceOf(ConflictException.class).hasMessageContaining("già conclusa");
    }
}
