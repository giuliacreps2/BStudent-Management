package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "mission_life_losses", indexes = {
        @Index(name = "idx_mission_life_losses_student", columnList = "student_id"),
        @Index(name = "idx_mission_life_losses_attempt", columnList = "mission_attempt_id, student_id")
})
// FK composta (mission_attempt_id, student_id) -> mission_attempts verificata dal DB
public class MissionLifeLoss {

    // La PK coincide con mission_attempt_id: al massimo una vita persa per tentativo
    @Id
    @Column(name = "mission_attempt_id")
    private Long missionAttemptId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "mission_attempt_id", nullable = false)
    private MissionAttempt missionAttempt;

    // student_id referenzia student_life_states, non students
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentLifeState studentLifeState;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime recordedAt;
}
