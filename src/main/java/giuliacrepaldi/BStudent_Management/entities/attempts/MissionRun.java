package giuliacrepaldi.BStudent_Management.entities.attempts;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionRunStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(
        name = "mission_runs",
        uniqueConstraints = {
                // Target della FK composta (mission_run_id, student_id) di mission_attempts
                @UniqueConstraint(name = "mission_runs_mission_run_id_student_id_idx", columnNames = {"mission_run_id", "student_id"})
        },
        indexes = {
                @Index(name = "idx_mission_runs_mission", columnList = "mission_id")
        }
)
// Indice unico parziale gestito solo dal DB:
// uq_open_mission_run (student_id) WHERE status = 'in_progress'
public class MissionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionRunId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private MissionRunStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
