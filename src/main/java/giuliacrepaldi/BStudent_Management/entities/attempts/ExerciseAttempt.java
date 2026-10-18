package giuliacrepaldi.BStudent_Management.entities.attempts;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(
        name = "exercise_attempts",
        uniqueConstraints = {
                // Target delle FK composte di mission_checkpoints
                @UniqueConstraint(name = "exercise_attempts_exercise_attempt_id_mission_attempt_id_idx",
                        columnNames = {"exercise_attempt_id", "mission_attempt_id"}),
                @UniqueConstraint(name = "uq_exercise_attempts_id_exercise",
                        columnNames = {"exercise_attempt_id", "mission_exercise_id"})
        },
        indexes = {
                @Index(name = "idx_exercise_attempts_entry", columnList = "question_bank_entry_id"),
                @Index(name = "idx_exercise_attempts_item", columnList = "mission_exercise_id"),
                @Index(name = "idx_exercise_attempts_history", columnList = "student_id, question_bank_entry_id, started_at DESC")
        }
)
@Check(name = "chk_exercise_attempts_context", constraints = """
        (context = 'mission' AND mission_attempt_id IS NOT NULL AND mission_exercise_id IS NOT NULL
            AND allowed_duration_ms IS NOT NULL AND remaining_time_ms IS NOT NULL)
        OR (context = 'practice' AND mission_attempt_id IS NULL AND mission_exercise_id IS NULL)
        """)
@Check(name = "chk_exercise_attempts_remaining_time",
        constraints = "remaining_time_ms IS NULL OR remaining_time_ms <= allowed_duration_ms")
@Check(name = "chk_exercise_attempts_timer", constraints = "timer_started_at IS NULL OR status = 'active'")
@Check(name = "chk_exercise_attempts_submitted",
        constraints = "status NOT IN ('pending_review','assessed','withdrawn') OR submitted_at IS NOT NULL")
@Check(name = "chk_exercise_attempts_completed",
        constraints = "(status IN ('assessed','abandoned','expired','withdrawn')) = (completed_at IS NOT NULL)")
// Indici unici parziali gestiti solo dal DB:
// uq_open_mission_exercise_attempt (mission_attempt_id) WHERE context = 'mission' AND status IN ('active','saved','pending_review')
// uq_open_practice_attempt (student_id, question_bank_entry_id) WHERE context = 'practice' AND status IN ('active','saved','pending_review')
public class ExerciseAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exerciseAttemptId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ExerciseContext context;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_entry_id", nullable = false)
    private QuestionBankEntry questionBankEntry;

    // Valorizzato solo con context = mission
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_attempt_id")
    private MissionAttempt missionAttempt;

    // Valorizzato solo con context = mission
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_exercise_id")
    private MissionExercise missionExercise;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ExerciseAttemptStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String response;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String configurationSnapshot;

    @Positive
    private Long allowedDurationMs;

    @PositiveOrZero
    private Long remainingTimeMs;

    private OffsetDateTime timerStartedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    private OffsetDateTime submittedAt;
    private OffsetDateTime completedAt;
}
