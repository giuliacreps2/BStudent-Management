package giuliacrepaldi.BStudent_Management.entities.skills;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "skill_levels", uniqueConstraints = {
        @UniqueConstraint(name = "skill_levels_skill_id_level_number_idx", columnNames = {"skill_id", "level_number"}),
        @UniqueConstraint(name = "skill_levels_skill_id_min_points_idx", columnNames = {"skill_id", "min_points"})
})
public class SkillLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skillLevelId;

    @Positive
    @Column(nullable = false)
    private int levelNumber;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minPoints;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
}
