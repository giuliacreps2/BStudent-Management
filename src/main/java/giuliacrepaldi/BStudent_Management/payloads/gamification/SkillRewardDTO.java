package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.math.BigDecimal;

// Punti skill massimi che una missione può dare in una skill
public record SkillRewardDTO(Long skillId, String skillName, BigDecimal maxPoints) {
}
