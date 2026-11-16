package giuliacrepaldi.BStudent_Management.payloads.gamification;

import giuliacrepaldi.BStudent_Management.entities.skills.EvolutionStageRule;
import giuliacrepaldi.BStudent_Management.enums.skills.EvolutionRuleKind;

import java.math.BigDecimal;

public record EvolutionStageRuleResponseDTO(int stageNumber, EvolutionRuleKind ruleKind, BigDecimal minXp,
                                            BigDecimal minAverageLevel) {

    public static EvolutionStageRuleResponseDTO from(EvolutionStageRule rule) {
        return new EvolutionStageRuleResponseDTO(rule.getStageNumber(), rule.getRuleKind(), rule.getMinXp(),
                rule.getMinAverageLevel());
    }
}
