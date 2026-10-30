package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    // Premium di default: un corso nuovo resta riservato finché non lo si segna free
    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private AccessTier accessTier = AccessTier.premium;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
