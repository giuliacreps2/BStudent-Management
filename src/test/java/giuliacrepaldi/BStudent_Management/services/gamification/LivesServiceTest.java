package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.gamification.GamificationSetting;
import giuliacrepaldi.BStudent_Management.entities.gamification.LifeAward;
import giuliacrepaldi.BStudent_Management.entities.gamification.MissionLifeLoss;
import giuliacrepaldi.BStudent_Management.entities.gamification.StudentLifeState;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.enums.gamification.LifeAwardReason;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.MissionAttemptsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.LifeAwardsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.MissionLifeLossesRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.StudentLifeStatesRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IGamificationSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Regole del blueprint principale §12.1 e del Blueprint Gamification §7.1, §8.1
class LivesServiceTest {

    private static final long STUDENT = 7L;
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 16);

    private StudentLifeStatesRepository lifeStates;
    private LifeAwardsRepository lifeAwards;
    private MissionLifeLossesRepository lifeLosses;
    private MissionAttemptsRepository attempts;
    private StudentsRepository students;
    private LivesService service;

    @BeforeEach
    void setUp() {
        lifeStates = mock(StudentLifeStatesRepository.class);
        lifeAwards = mock(LifeAwardsRepository.class);
        lifeLosses = mock(MissionLifeLossesRepository.class);
        attempts = mock(MissionAttemptsRepository.class);
        students = mock(StudentsRepository.class);
        IGamificationSettingsService settings = mock(IGamificationSettingsService.class);
        when(settings.get()).thenReturn(GamificationSetting.builder().baseLives(5).lifeRechargeHours(10).build());
        when(lifeStates.save(any(StudentLifeState.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new LivesService(lifeStates, lifeAwards, lifeLosses, attempts, students, settings);
    }

    private StudentLifeState state(int lives, int base, OffsetDateTime rechargeAt) {
        StudentLifeState state = StudentLifeState.builder()
                .studentId(STUDENT).lives(lives).baseLives(base).rechargeAt(rechargeAt).build();
        when(lifeStates.findById(STUDENT)).thenReturn(Optional.of(state));
        return state;
    }

    private void attemptOf(long attemptId, long studentId) {
        Student student = new Student();
        student.setStudentId(studentId);
        MissionAttempt attempt = new MissionAttempt();
        attempt.setMissionAttemptId(attemptId);
        attempt.setStudent(student);
        when(attempts.findById(attemptId)).thenReturn(Optional.of(attempt));
    }

    @Test
    void allaPrimaLetturaLoStudenteRiceveLeViteDiBase() {
        when(lifeStates.findById(STUDENT)).thenReturn(Optional.empty());
        when(students.findById(STUDENT)).thenReturn(Optional.of(new Student()));
        assertThat(service.getLives(STUDENT)).isEqualTo(new LivesDTO(5, 5, null));
    }

    @Test
    void laRicaricaScadutaPortaIlSaldoAllaBaseESpegneIlTimer() {
        state(3, 5, OffsetDateTime.now().minusMinutes(1));
        assertThat(service.getLives(STUDENT)).isEqualTo(new LivesDTO(5, 5, null));
    }

    @Test
    void laRicaricaNonScadutaNonCambiaNulla() {
        OffsetDateTime later = OffsetDateTime.now().plusHours(3);
        state(3, 5, later);
        assertThat(service.getLives(STUDENT)).isEqualTo(new LivesDTO(3, 5, later));
    }

    @Test
    void unaVitaPremioSuperaLaBaseENonRiavviaLaRicarica() {
        OffsetDateTime later = OffsetDateTime.now().plusHours(3);
        state(5, 5, later);
        LivesDTO lives = service.awardLife(STUDENT, LifeAwardReason.practice_streak, TODAY, "tris:1");
        assertThat(lives).isEqualTo(new LivesDTO(6, 5, later));
        verify(lifeAwards).save(any(LifeAward.class));
    }

    @Test
    void loStessoEventoNonAssegnaDueVite() {
        state(5, 5, null);
        when(lifeAwards.existsByEventId("tris:1")).thenReturn(true);
        assertThat(service.awardLife(STUDENT, LifeAwardReason.practice_streak, TODAY, "tris:1").lives()).isEqualTo(5);
        verify(lifeAwards, never()).save(any(LifeAward.class));
    }

    @Test
    void laSfidaGiornalieraDaUnaVitaSolaAlGiorno() {
        state(5, 5, null);
        when(lifeAwards.existsByStudentIdAndReasonAndAwardDay(STUDENT, LifeAwardReason.daily_challenge, TODAY))
                .thenReturn(true);
        assertThat(service.awardLife(STUDENT, LifeAwardReason.daily_challenge, TODAY, "sfida:altro").lives())
                .isEqualTo(5);
        verify(lifeAwards, never()).save(any(LifeAward.class));
    }

    @Test
    void laPrimaEvoluzionePortaBaseESaldoASetteSoloUnaVolta() {
        StudentLifeState state = state(3, 5, null);
        assertThat(service.raiseBaseLives(STUDENT, 7)).isEqualTo(new LivesDTO(7, 7, null));
        // Ripetuta: nessun effetto, anche se nel frattempo il saldo è sceso
        state.setLives(4);
        assertThat(service.raiseBaseLives(STUDENT, 7)).isEqualTo(new LivesDTO(4, 7, null));
    }

    @Test
    void leViteExtraRestanoQuandoLaBaseSale() {
        state(9, 5, null);
        assertThat(service.raiseBaseLives(STUDENT, 7)).isEqualTo(new LivesDTO(9, 7, null));
    }

    @Test
    void unTentativoFallitoToglieUnaVitaEAvviaLaRicaricaDiDieciOre() {
        state(5, 5, null);
        attemptOf(40L, STUDENT);
        OffsetDateTime before = OffsetDateTime.now();
        LivesDTO lives = service.onMissionAttemptFailed(STUDENT, 40L);
        assertThat(lives.lives()).isEqualTo(4);
        assertThat(lives.rechargeAt()).isBetween(before.plusHours(10), OffsetDateTime.now().plusHours(10));
        verify(lifeLosses).save(any(MissionLifeLoss.class));
    }

    @Test
    void laSecondaVitaPersaNonRiavviaIlTimer() {
        OffsetDateTime running = OffsetDateTime.now().plusHours(4);
        state(4, 5, running);
        attemptOf(41L, STUDENT);
        assertThat(service.onMissionAttemptFailed(STUDENT, 41L)).isEqualTo(new LivesDTO(3, 5, running));
    }

    @Test
    void loStessoTentativoFallitoNonToglieDueVite() {
        state(4, 5, OffsetDateTime.now().plusHours(4));
        when(lifeLosses.existsById(40L)).thenReturn(true);
        assertThat(service.onMissionAttemptFailed(STUDENT, 40L).lives()).isEqualTo(4);
        verify(lifeLosses, never()).save(any(MissionLifeLoss.class));
    }

    @Test
    void senzaViteNonSiIniziaUnaMissione() {
        state(0, 5, OffsetDateTime.now().plusHours(4));
        assertThat(service.canStartMission(STUDENT)).isFalse();
        state(1, 5, OffsetDateTime.now().plusHours(4));
        assertThat(service.canStartMission(STUDENT)).isTrue();
    }
}
