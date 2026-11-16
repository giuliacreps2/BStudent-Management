package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.enums.skills.EvolutionRuleKind;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "evolution_stage_rules")
// Regola di uno stadio dell'evoluzione, uguale per tutti i personaggi (v0.10, Blueprint Gamification §4).
// Le immagini di ogni personaggio restano in skin_evolutions
public class EvolutionStageRule {

    // 0 = Base, 1 = Evoluzione I, 2 = Evoluzione II...
    @Id
    @Column(name = "stage_number")
    private Integer stageNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private EvolutionRuleKind ruleKind;

    // Valorizzati se e solo se ruleKind = thresholds (chk_evolution_stage_rules_thresholds)
    @Column(precision = 12, scale = 2)
    private BigDecimal minXp;

    @Column(precision = 4, scale = 2)
    private BigDecimal minAverageLevel;
}
