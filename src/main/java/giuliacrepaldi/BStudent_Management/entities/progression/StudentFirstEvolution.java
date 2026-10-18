package giuliacrepaldi.BStudent_Management.entities.progression;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.skills.Skin;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "student_first_evolutions", indexes = {
        @Index(name = "idx_first_evolutions_course", columnList = "course_id"),
        @Index(name = "idx_first_evolutions_skin", columnList = "skin_id")
})
public class StudentFirstEvolution {

    // Una sola prima evoluzione per studente: la PK coincide con student_id
    @Id
    @Column(name = "student_id")
    private Long studentId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime awardedAt;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleVersion = "v0.1_first_course";

    @NotBlank
    @Column(name = "event_id", nullable = false, unique = true, columnDefinition = "TEXT")
    private String eventId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_id", nullable = false)
    private Skin skin;
}
