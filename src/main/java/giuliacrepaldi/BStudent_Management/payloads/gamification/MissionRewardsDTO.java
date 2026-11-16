package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.math.BigDecimal;
import java.util.List;

// Ricompense mostrate sulla missione (blueprint §5.9): "+N XP" e punti skill massimi per skill
public record MissionRewardsDTO(BigDecimal xp, List<SkillRewardDTO> skillPoints) {
}
