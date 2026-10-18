package giuliacrepaldi.BStudent_Management.entities.gamification;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "accessory_benefits")
public class AccessoryBenefit {

    // La PK coincide con accessory_id: un solo benefit per accessorio
    @Id
    @Column(name = "accessory_id")
    private Long accessoryId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "accessory_id", nullable = false)
    private Accessory accessory;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String kind;

    @Positive
    @Column(nullable = false)
    private int capacity;

    @Positive
    @Column(nullable = false)
    private int rechargeHours;
}
