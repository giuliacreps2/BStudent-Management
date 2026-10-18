package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.Kind;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "lesson_pages", uniqueConstraints = {
        @UniqueConstraint(name = "lesson_pages_lesson_id_sort_order_idx", columnNames = {"lesson_id", "sort_order"})
})
public class LessonPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lessonPageId;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int lessonPageSortOrder;

    @Column(columnDefinition = "TEXT")
    private String formattedText;

    @Column(columnDefinition = "TEXT")
    private String imageReference;

    @Column(columnDefinition = "TEXT")
    private String videoProvider;

    @Column(columnDefinition = "TEXT")
    private String videoIdentifier;

    // Derivato dal contenuto valorizzato in resolveKind()
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private Kind kind;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @PrePersist
    @PreUpdate
    private void resolveKind() {
        boolean hasText = formattedText != null;
        boolean hasImage = imageReference != null;
        boolean hasVideo = videoProvider != null || videoIdentifier != null;

        if (hasText && !hasImage && !hasVideo) {
            kind = Kind.text;
        } else if (hasImage && !hasText && !hasVideo) {
            kind = Kind.image;
        } else if (hasVideo && videoProvider != null && videoIdentifier != null && !hasText && !hasImage) {
            kind = Kind.video;
        } else {
            throw new IllegalStateException(
                    "A lesson page must contain exactly one content type: text, image or video (provider + identifier)");
        }
    }
}
