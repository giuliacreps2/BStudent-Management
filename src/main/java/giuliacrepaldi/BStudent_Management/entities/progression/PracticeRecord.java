package giuliacrepaldi.BStudent_Management.entities.progression;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(
        name = "practice_records",
        uniqueConstraints = {
                // Un giudizio alimenta un solo record
                @UniqueConstraint(name = "uq_practice_records_attempt", columnNames = "exercise_attempt_id")
        },
        indexes = {
                @Index(name = "idx_practice_records_exercise", columnList = "practice_exercise_id"),
                @Index(name = "idx_practice_records_skill", columnList = "skill_id")
        }
)
@Check(name = "chk_practice_records_points_range", constraints = "points >= 0 AND points <= max_points_snapshot")
@Check(name = "chk_practice_records_points_formula",
        constraints = "points = round(max_points_snapshot * percentage / 100, 2)")
public class PracticeRecord {

    // Chiave composta: (student_id, practice_exercise_id), il miglior risultato per esercizio
    @EmbeddedId
    @Builder.Default
    private PracticeRecordId id = new PracticeRecordId();

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ToString.Exclude
    @MapsId("practiceExerciseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_exercise_id")
    private PracticeExercise practiceExercise;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    // exercise_attempt_id referenzia exercise_assessments: il record nasce da un giudizio
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_attempt_id", nullable = false)
    private ExerciseAssessment exerciseAssessment;

    // Solo esercizi superati entrano nel record
    @NotNull
    @DecimalMin("60")
    @DecimalMax("100")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal maxPointsSnapshot;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal points;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void validatePointsFormula() {
        if (maxPointsSnapshot == null || percentage == null || points == null) {
            return;
        }
        BigDecimal expected = maxPointsSnapshot.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (points.compareTo(expected) != 0) {
            throw new IllegalStateException("points (%s) must equal maxPointsSnapshot * percentage / 100 (%s)"
                    .formatted(points, expected));
        }
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PracticeRecordId implements Serializable {
        private Long studentId;
        private Long practiceExerciseId;
    }
}
