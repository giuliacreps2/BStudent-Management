package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.Kind;
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
@Table(indexes = {
        @Index(name = "lesson_id", columnList = "sort_order", unique = true)
})
public class LessonsPages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long lessonPageId;

    @Column(name = "sort_order", nullable = false)
    private int lessonPageSortOrder;
    private String formattedText;
    private String imageReference;
    private String videoProvider;
    private String videoIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind = validateKind();

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lessons lesson;

    private void validateLessonPageSortOrder() {
        if (this.lessonPageSortOrder < 0) {
            log.info("Lesson Page sort order cannot be less than 0");
        }
    }

    private Kind validateKind() {
        if (formattedText != null && imageReference == null && videoProvider == null && videoIdentifier == null) {
            return Kind.text;
        } else if (imageReference != null && formattedText == null && videoProvider == null && videoIdentifier == null) {
            return Kind.image;
        } else if (videoProvider != null && videoIdentifier != null && formattedText == null && imageReference == null) {
            return Kind.video;
        } else {
            return null;
        }
    }
}
