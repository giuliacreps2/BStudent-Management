package giuliacrepaldi.BStudent_Management.entities.gamification;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Slf4j

@Entity
public class GamificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settings_id")
    private short gamificationSettingsId;

    @Column(nullable = false)
    private int baseLives;

    @Column(nullable = false)
    private int lifeRechargeHours;


    private void validateBaseLives() {
        if (this.baseLives < 0) {
            log.info("The maximum allowed points can not be negative");
        }
    }

    private void validateLifeRechargeHours() {
        if (this.lifeRechargeHours > 0) {
            log.info("Life Recharge Hours can start from 0");
        }
    }
}
