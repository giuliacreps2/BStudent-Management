package giuliacrepaldi.BStudent_Management.payloads.gamification;

import giuliacrepaldi.BStudent_Management.entities.skills.SkillLevel;

import java.math.BigDecimal;

public record SkillLevelResponseDTO(int levelNumber, BigDecimal minPoints) {

    public static SkillLevelResponseDTO from(SkillLevel level) {
        return new SkillLevelResponseDTO(level.getLevelNumber(), level.getMinPoints());
    }
}
