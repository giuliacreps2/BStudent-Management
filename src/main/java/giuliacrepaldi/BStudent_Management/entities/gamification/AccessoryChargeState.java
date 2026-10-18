package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.Check;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "accessory_charge_states")
// FK composta (student_id, accessory_id) -> student_accessories verificata dal DB
@Check(name = "chk_accessory_charge_states_recharge", constraints = """
        (remaining_charges = 0 AND recharge_at IS NOT NULL)
        OR (remaining_charges > 0 AND recharge_at IS NULL)
        """)
public class AccessoryChargeState {

    // Chiave composta: (student_id, accessory_id)
    @EmbeddedId
    @Builder.Default
    private AccessoryChargeStateId id = new AccessoryChargeStateId();

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    // accessory_id referenzia accessory_benefits: solo gli accessori con cariche
    @ToString.Exclude
    @MapsId("accessoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessory_id")
    private AccessoryBenefit accessoryBenefit;

    @PositiveOrZero
    @Column(nullable = false)
    private int remainingCharges;

    private OffsetDateTime rechargeAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class AccessoryChargeStateId implements Serializable {
        private Long studentId;
        private Long accessoryId;
    }
}
