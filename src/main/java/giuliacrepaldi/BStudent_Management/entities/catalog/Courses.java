package giuliacrepaldi.BStudent_Management.entities.catalog;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Slf4j

@Entity
public class Courses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long courseId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String courseTitle;

    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String courseShortDescription;

    @Column(name = "extended_description", nullable = false, columnDefinition = "TEXT")
    private String courseDescription;

    @Column(name = "cover_reference")
    private String courseImage;


    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subjects subject;

    private void validateCourseTitle() {
        if (this.courseTitle != null && !this.courseTitle.trim().isEmpty()) {
            log.info("Course title already exists {}", courseTitle.trim());
        } else {
            log.info("Course Title is null or empty");
        }
    }
}
