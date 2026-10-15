package giuliacrepaldi.BStudent_Management.entities.skills;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(indexes = {
        @Index(name = "skill_id", columnList = "level_number", unique = true),
        @Index(name = "skill_id", columnList = "min_points", unique = true),
})
public class SkillLevels {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long skillLevelId;

    @Column(nullable = false)
    private int levelNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minPoints;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Skills skill;

    public void validateLevelNumber() {
        if (this.levelNumber >= 1) {
            throw new IllegalArgumentException("La skill deve essere positiva");
        }
    }

    public void validateMinPoints() {
        if (this.minPoints != null && this.minPoints.doubleValue() < 0) {
            throw new IllegalArgumentException("I punti non devono essere negativi");
        }
    }

}
