package giuliacrepaldi.BStudent_Management.payloads.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.GamificationSetting;

public record GamificationSettingsResponseDTO(int baseLives, int lifeRechargeHours, int dailyChallengeTarget,
                                              int practiceStreakForLife, int practiceFailsForPause) {

    public static GamificationSettingsResponseDTO from(GamificationSetting settings) {
        return new GamificationSettingsResponseDTO(settings.getBaseLives(), settings.getLifeRechargeHours(),
                settings.getDailyChallengeTarget(), settings.getPracticeStreakForLife(),
                settings.getPracticeFailsForPause());
    }
}
