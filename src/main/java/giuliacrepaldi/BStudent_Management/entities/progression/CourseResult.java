package giuliacrepaldi.BStudent_Management.entities.progression;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.Check;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "course_results", indexes = {
        @Index(name = "idx_course_results_course", columnList = "course_id")
})
@Check(name = "chk_course_results_passed_le_total", constraints = "passed_missions <= total_missions")
@Check(name = "chk_course_results_completed",
        constraints = "completed_at IS NULL OR passed_missions = total_missions")
public class CourseResult {

    // Chiave composta: (student_id, course_id)
    @EmbeddedId
    @Builder.Default
    private CourseResultId id = new CourseResultId();

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ToString.Exclude
    @MapsId("courseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @PositiveOrZero
    @Column(nullable = false)
    private int passedMissions;

    @Positive
    @Column(nullable = false)
    private int totalMissions;

    @DecimalMin("0")
    @DecimalMax("100")
    @Column(precision = 5, scale = 2)
    private BigDecimal percentage;

    private OffsetDateTime completedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CourseResultId implements Serializable {
        private Long studentId;
        private Long courseId;
    }
}
