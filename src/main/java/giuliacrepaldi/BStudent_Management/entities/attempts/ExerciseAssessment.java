package giuliacrepaldi.BStudent_Management.entities.attempts;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(
        name = "exercise_assessments",
        uniqueConstraints = {
                // Target della FK composta (exercise_attempt_id, passed) di mission_checkpoints
                @UniqueConstraint(name = "uq_exercise_assessments_attempt_passed", columnNames = {"exercise_attempt_id", "passed"})
        },
        indexes = {
                @Index(name = "idx_exercise_assessments_teacher", columnList = "teacher_user_id")
        }
)
@Check(name = "chk_exercise_assessments_teacher",
        constraints = "(assessment_method = 'teacher') = (teacher_user_id IS NOT NULL)")
@Check(name = "chk_exercise_assessments_passed",
        constraints = "assessment_method <> 'automatic' OR (passed IS NOT NULL AND passed = (percentage >= passing_threshold))")
public class ExerciseAssessment {

    // La PK coincide con exercise_attempt_id: un solo giudizio per tentativo
    @Id
    @Column(name = "exercise_attempt_id")
    private Long exerciseAttemptId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "exercise_attempt_id", nullable = false)
    private ExerciseAttempt exerciseAttempt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private AssessmentMethod assessmentMethod;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal passingThreshold;

    // Automatico: deve valere percentage >= passing_threshold (chk_exercise_assessments_passed).
    // Docente (e in futuro AI): valorizzato liberamente
    private Boolean passed;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    // Valorizzato solo con assessment_method = teacher
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_user_id")
    private User teacher;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime validatedAt;
}
