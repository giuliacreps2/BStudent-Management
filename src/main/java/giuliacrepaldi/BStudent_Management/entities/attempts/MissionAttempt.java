package giuliacrepaldi.BStudent_Management.entities.attempts;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.enums.attempts.FailureReason;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "mission_attempts", uniqueConstraints = {
        @UniqueConstraint(name = "mission_attempts_mission_run_id_attempt_number_idx", columnNames = {"mission_run_id", "attempt_number"}),
        // Target delle FK composte di mission_checkpoints ed exercise_attempts/hint_uses/mission_life_losses
        @UniqueConstraint(name = "mission_attempts_mission_attempt_id_mission_run_id_idx", columnNames = {"mission_attempt_id", "mission_run_id"}),
        @UniqueConstraint(name = "uq_mission_attempts_id_student", columnNames = {"mission_attempt_id", "student_id"})
})
@Check(name = "chk_mission_attempts_completed",
        constraints = "(status IN ('failed','passed')) = (completed_at IS NOT NULL)")
@Check(name = "chk_mission_attempts_failure_reason",
        constraints = "(status = 'failed') = (failure_reason IS NOT NULL)")
// Indice unico parziale gestito solo dal DB:
// uq_open_mission_attempt (mission_run_id) WHERE status IN ('active','saved','pending_review')
public class MissionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionAttemptId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_run_id", nullable = false)
    private MissionRun missionRun;

    // Ridondante rispetto a missionRun.student: richiesto dalla FK composta (mission_run_id, student_id)
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Positive
    @Column(nullable = false)
    private int attemptNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private MissionAttemptStatus status;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private FailureReason failureReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    private OffsetDateTime completedAt;

    @PrePersist
    @PreUpdate
    private void validateStudentMatchesRun() {
        if (missionRun == null || student == null) {
            return;
        }
        if (!Objects.equals(missionRun.getStudent().getStudentId(), student.getStudentId())) {
            throw new IllegalStateException("The attempt must belong to the same student of the mission run");
        }
    }
}
