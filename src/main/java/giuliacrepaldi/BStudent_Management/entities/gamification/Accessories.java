package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.entities.skills.Skills;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Slf4j

@Entity
@Table(
        indexes = {
                @Index(name = "accessory_id", columnList = "slot_code", unique = true)
        }
)
public class Accessories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accessoryId;

    @Column(name = "code", nullable = false, unique = true)
    private String accessoryCode;

    @Column(name = "name", nullable = false)
    private String accessoryName;

    @Column(name = "slot_code", nullable = false)
    private String accessorySlotCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal requiredPoints;

    @Column(nullable = false)
    private boolean isAvailable = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skills skill;

    private void validateAccessoryCode(String accessoryCode) {
        if (this.accessoryCode != null && !this.accessoryCode.trim().isEmpty()) {
            log.info("Accessory code {} already exists", accessoryCode);
        }
    }

    private void validateAccessoryName(String accessoryName) {
        if (this.accessoryName != null && !this.accessoryName.trim().isEmpty()) {
            log.info("Accessory name {} already exists", accessoryName);
        }
    }

    private void validateAccessorySlotCode(String accessorySlotCode) {
        if (this.accessorySlotCode != null && !this.accessorySlotCode.trim().isEmpty()) {
            log.info("Accessory slot code {} already exists", accessorySlotCode);
        }
    }

    private void validateRequiredPoints(BigDecimal requiredPoints) {
        if (requiredPoints.compareTo(BigDecimal.ZERO) < 0) {
            log.info("Required points can not be negative");
        }
    }
}
