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
@Table(name = "life_recovery_activities")
public class LifeRecoveryActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String guidedSolution;

    @Positive
    @Column(nullable = false)
    private int difficulty;

    @Builder.Default
    @Column(nullable = false)
    private boolean isAvailable = true;
}
