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
}
