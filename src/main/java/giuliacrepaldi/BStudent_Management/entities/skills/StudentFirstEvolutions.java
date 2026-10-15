package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.entities.catalog.Courses;
import giuliacrepaldi.BStudent_Management.entities.identity.Students;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
public class StudentFirstEvolutions {

    @Column(nullable = false)
    private OffsetDateTime awardedAt = OffsetDateTime.now();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleVersion = "v0.1_first_course";

    @Column(name = "event_id", nullable = false, columnDefinition = "TEXT")
    private String eventId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Students student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Courses course;

}
