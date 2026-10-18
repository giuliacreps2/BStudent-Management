package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Table(
        name = "accessories",
        uniqueConstraints = {
                // Target della FK composta (accessory_id, slot_code) di equipped_accessories
                @UniqueConstraint(name = "accessories_accessory_id_slot_code_idx", columnNames = {"accessory_id", "slot_code"})
        },
        indexes = {
                @Index(name = "idx_accessories_skill", columnList = "skill_id")
        }
)
public class Accessory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accessoryId;

    @NotBlank
    @Column(name = "code", nullable = false, unique = true, columnDefinition = "TEXT")
    private String accessoryCode;

    @NotBlank
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String accessoryName;

    @NotBlank
    @Column(name = "slot_code", nullable = false, columnDefinition = "TEXT")
    private String accessorySlotCode;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal requiredPoints;

    @Builder.Default
    @Column(nullable = false)
    private boolean isAvailable = true;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
}
