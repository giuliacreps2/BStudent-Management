package giuliacrepaldi.BStudent_Management.entities.gamification;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "gamification_settings")
public class GamificationSetting {

    // Riga singola di configurazione: il DB impone settings_id = 1 (nessuna IDENTITY)
    @Id
    @Column(name = "settings_id")
    private Short gamificationSettingsId;

    @Positive
    @Column(nullable = false)
    private int baseLives;

    @Positive
    @Column(nullable = false)
    private int lifeRechargeHours;

    // Voci della Palestra da superare nel giorno per la sfida giornaliera (v0.10, Blueprint Gamification §7.1)
    @Positive
    @Builder.Default
    @Column(nullable = false)
    private int dailyChallengeTarget = 3;

    // Voci superate di fila per ottenere una vita (§8.1)
    @Positive
    @Builder.Default
    @Column(nullable = false)
    private int practiceStreakForLife = 3;

    // Errori di fila dopo i quali la Palestra propone la pausa (§8.1-bis)
    @Positive
    @Builder.Default
    @Column(nullable = false)
    private int practiceFailsForPause = 3;
}
