package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.GamificationSetting;
import giuliacrepaldi.BStudent_Management.enums.gamification.LifeAwardReason;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeRewardsDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StreakDTO;
import giuliacrepaldi.BStudent_Management.repositories.attempts.ExerciseAssessmentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.gamification.LifeAwardsRepository;
import giuliacrepaldi.BStudent_Management.repositories.practice.PracticeExercisesRepository;
import giuliacrepaldi.BStudent_Management.repositories.resources.ResourcesRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IGamificationSettingsService;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ILivesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Sfida giornaliera, tris, Serie e pausa (Blueprint Gamification §7, §8.1, §8.1-bis)
class DailyGoalsServiceTest {

    private static final long STUDENT = 7L;
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    private ExerciseAssessmentsRepository assessments;
    private ILivesService lives;
    private DailyGoalsService service;
    private final List<Object[]> rows = new ArrayList<>();
    // Mezzogiorno di oggi in Italia: tutti i tentativi dei test cadono nello stesso giorno
    private final ZonedDateTime noon = ZonedDateTime.now(ROME).withHour(12).withMinute(0).withSecond(0).withNano(0);
    private final LocalDate today = noon.toLocalDate();

    @BeforeEach
    void setUp() {
        assessments = mock(ExerciseAssessmentsRepository.class);
        lives = mock(ILivesService.class);
        IGamificationSettingsService settings = mock(IGamificationSettingsService.class);
        when(settings.get()).thenReturn(GamificationSetting.builder().baseLives(5).lifeRechargeHours(10).build());
        when(assessments.findPracticeOutcomes(STUDENT)).thenReturn(rows);
        when(lives.getLives(STUDENT)).thenReturn(new LivesDTO(5, 5, null));
        when(lives.awardLife(anyLong(), any(), any(), anyString())).thenReturn(new LivesDTO(6, 5, null));
        PracticeExercisesRepository practiceExercises = mock(PracticeExercisesRepository.class);
        when(practiceExercises.findById(anyLong())).thenReturn(Optional.empty());
        service = new DailyGoalsService(assessments, mock(LifeAwardsRepository.class), practiceExercises,
                mock(ResourcesRepository.class), lives, settings);
    }

    // Riga della query: tentativo, voce della Palestra, istante in secondi, superato
    private void attempt(long attemptId, long practiceExerciseId, boolean passed) {
        rows.add(new Object[]{attemptId, practiceExerciseId,
                new BigDecimal(noon.plusMinutes(rows.size()).toEpochSecond()), passed});
    }

    @Test
    void ilTerzoSuperatoDiFilaDaLaVitaDelTrisEQuellaDellaSfida() {
        attempt(1, 100, true);
        attempt(2, 101, true);
        attempt(3, 102, true);

        PracticeRewardsDTO rewards = service.onPracticeAttemptEvaluated(STUDENT, 3);

        // Primo tris del giorno: 2 vite, una del tris e una della sfida (§8.1)
        assertThat(rewards.streakLifeAwarded()).isTrue();
        assertThat(rewards.dailyChallengeLifeAwarded()).isTrue();
        assertThat(rewards.dailyChallenge().completed()).isTrue();
        assertThat(rewards.run().passedInARow()).isZero();
        verify(lives).awardLife(STUDENT, LifeAwardReason.practice_streak, today, "practice_streak:3");
        verify(lives).awardLife(STUDENT, LifeAwardReason.daily_challenge, today, "daily_challenge:7:" + today);
    }

    @Test
    void ilSecondoSuperatoNonDaAncoraNulla() {
        attempt(1, 100, true);
        attempt(2, 101, true);

        PracticeRewardsDTO rewards = service.onPracticeAttemptEvaluated(STUDENT, 2);

        assertThat(rewards.streakLifeAwarded()).isFalse();
        assertThat(rewards.dailyChallengeLifeAwarded()).isFalse();
        assertThat(rewards.run().passedInARow()).isEqualTo(2);
        assertThat(rewards.dailyChallenge().passedToday()).isEqualTo(2);
        verify(lives, never()).awardLife(anyLong(), any(), any(), anyString());
    }

    @Test
    void unErroreAzzeraIlTrisMaLaSfidaContaComunqueITreSuperati() {
        attempt(1, 100, true);
        attempt(2, 101, true);
        attempt(3, 102, false);
        attempt(4, 103, true);

        PracticeRewardsDTO rewards = service.onPracticeAttemptEvaluated(STUDENT, 4);

        // Tre voci superate oggi: sfida completata; il tris è ripartito dopo l'errore
        assertThat(rewards.dailyChallengeLifeAwarded()).isTrue();
        assertThat(rewards.streakLifeAwarded()).isFalse();
        assertThat(rewards.run().passedInARow()).isEqualTo(1);
        verify(lives, never()).awardLife(eq(STUDENT), eq(LifeAwardReason.practice_streak), any(), anyString());
    }

    @Test
    void ripetereLaStessaVoceNonPortaAlTris() {
        attempt(1, 100, true);
        attempt(2, 100, true);
        attempt(3, 100, true);

        PracticeRewardsDTO rewards = service.onPracticeAttemptEvaluated(STUDENT, 3);

        assertThat(rewards.streakLifeAwarded()).isFalse();
        assertThat(rewards.dailyChallenge().passedToday()).isEqualTo(1);
    }

    @Test
    void iTentativiValutatiDopoNonCambianoIlPremioDiUnTentativoPrecedente() {
        // La docente valuta il tentativo 2 quando il 3 e il 4 esistono già: conta lo stato fino al 2
        attempt(1, 100, true);
        attempt(2, 101, true);
        attempt(3, 102, true);
        attempt(4, 103, true);

        assertThat(service.onPracticeAttemptEvaluated(STUDENT, 2).streakLifeAwarded()).isFalse();
    }

    @Test
    void dopoTreErroriDiFilaArrivaLInvitoAllaPausa() {
        attempt(1, 100, false);
        attempt(2, 101, false);
        assertThat(service.getPracticeRunState(STUDENT).pause()).isNull();

        attempt(3, 102, false);
        assertThat(service.getPracticeRunState(STUDENT).pause().message()).contains("pausa");

        // Al quarto errore l'invito non si ripete: torna al sesto
        attempt(4, 103, false);
        assertThat(service.getPracticeRunState(STUDENT).pause()).isNull();
    }

    @Test
    void laSerieSiCalcolaDaiGiorniDiStudio() {
        when(assessments.findStudyDays(STUDENT)).thenReturn(List.of(
                today.minusDays(4).toString(), today.minusDays(3).toString(), today.minusDays(1).toString(), today.toString()));
        assertThat(service.getStreak(STUDENT)).isEqualTo(new StreakDTO(4, 4));
    }

    @Test
    void unTentativoNonValutatoNonDaPremi() {
        attempt(1, 100, true);
        assertThatThrownBy(() -> service.onPracticeAttemptEvaluated(STUDENT, 99))
                .isInstanceOf(NotFoundException.class);
    }
}
