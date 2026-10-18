package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.enums.gamification.LifeRecoveryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "life_recovery_attempts", indexes = {
        @Index(name = "idx_life_recovery_attempts_activity", columnList = "activity_id")
})
@Check(name = "chk_life_recovery_attempts_response_not_blank",
        constraints = "response IS NULL OR btrim(response) <> ''")
@Check(name = "chk_life_recovery_attempts_response_required",
        constraints = "status = 'started' OR response IS NOT NULL")
@Check(name = "chk_life_recovery_attempts_completion", constraints = """
        (status = 'completed' AND self_assessed_passed IS NOT NULL AND completed_at IS NOT NULL)
        OR (status <> 'completed' AND self_assessed_passed IS NULL AND completed_at IS NULL)
        """)
@Check(name = "chk_life_recovery_attempts_life_awarded", constraints = """
        (life_awarded_at IS NOT NULL AND status = 'completed' AND self_assessed_passed IS TRUE)
        OR (life_awarded_at IS NULL AND self_assessed_passed IS DISTINCT FROM TRUE)
        """)
// Indice unico parziale gestito solo dal DB:
// uq_open_life_recovery_attempt (student_id) WHERE status <> 'completed'
public class LifeRecoveryAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recoveryAttemptId;

    // student_id referenzia student_life_states, non students
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentLifeState studentLifeState;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private LifeRecoveryActivity activity;

    @Positive
    @Column(nullable = false)
    private int assignedDifficulty;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private LifeRecoveryStatus status = LifeRecoveryStatus.started;

    @Column(columnDefinition = "TEXT")
    private String response;

    private Boolean selfAssessedPassed;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    private OffsetDateTime completedAt;
    private OffsetDateTime lifeAwardedAt;
}
