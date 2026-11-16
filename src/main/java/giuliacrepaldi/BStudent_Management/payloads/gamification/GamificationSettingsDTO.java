package giuliacrepaldi.BStudent_Management.payloads.gamification;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Impostazioni di gioco modificabili dall'Admin (Blueprint Gamification §9)
public record GamificationSettingsDTO(
        @NotNull @Positive @Max(20) Integer baseLives,
        @NotNull @Positive @Max(168) Integer lifeRechargeHours,
        @NotNull @Positive @Max(20) Integer dailyChallengeTarget,
        @NotNull @Positive @Max(20) Integer practiceStreakForLife,
        @NotNull @Positive @Max(20) Integer practiceFailsForPause) {
}
