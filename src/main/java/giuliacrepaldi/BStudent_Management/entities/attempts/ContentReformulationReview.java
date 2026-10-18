package giuliacrepaldi.BStudent_Management.entities.attempts;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.attempts.ReformulationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "content_reformulation_reviews", indexes = {
        @Index(name = "idx_reformulation_reviews_teacher", columnList = "teacher_user_id")
})
@Check(name = "chk_reformulation_reviews_proposal",
        constraints = "status <> 'proposal_available' OR ai_proposal IS NOT NULL")
@Check(name = "chk_reformulation_reviews_validation", constraints = """
        (status = 'validated' AND teacher_feedback IS NOT NULL AND btrim(teacher_feedback) <> ''
            AND teacher_percentage IS NOT NULL AND teacher_user_id IS NOT NULL AND validated_at IS NOT NULL)
        OR (status <> 'validated' AND teacher_feedback IS NULL AND teacher_percentage IS NULL
            AND teacher_user_id IS NULL AND validated_at IS NULL)
        """)
public class ContentReformulationReview {

    // La PK coincide con exercise_attempt_id: una sola revisione per tentativo
    @Id
    @Column(name = "exercise_attempt_id")
    private Long exerciseAttemptId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "exercise_attempt_id", nullable = false)
    private ExerciseAttempt exerciseAttempt;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String studentText;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String selectedContext;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ReformulationStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String aiProposal;

    @Column(columnDefinition = "TEXT")
    private String modelReference;

    @Column(columnDefinition = "TEXT")
    private String instructionsVersion;

    @Column(columnDefinition = "TEXT")
    private String teacherFeedback;

    @DecimalMin("0")
    @DecimalMax("100")
    @Column(precision = 5, scale = 2)
    private BigDecimal teacherPercentage;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_user_id")
    private User teacher;

    private OffsetDateTime validatedAt;
}
