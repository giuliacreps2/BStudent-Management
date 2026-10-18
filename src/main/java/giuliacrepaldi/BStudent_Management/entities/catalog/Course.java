package giuliacrepaldi.BStudent_Management.entities.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_courses_subject", columnList = "subject_id")
})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    @NotBlank
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String courseTitle;

    @NotBlank
    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String courseShortDescription;

    @NotBlank
    @Column(name = "extended_description", nullable = false, columnDefinition = "TEXT")
    private String courseDescription;

    @Column(name = "cover_reference", columnDefinition = "TEXT")
    private String courseImage;

    private OffsetDateTime withdrawnAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
