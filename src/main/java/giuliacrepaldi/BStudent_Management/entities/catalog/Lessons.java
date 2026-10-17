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
@Table(indexes = {
        @Index(name = "mission_id", columnList = "sort_order", unique = true),
        @Index(name = "lesson_id", columnList = "mission_id", unique = true)
})
public class Lessons {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long lessonId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String lessonTitle;

    @Column(name = "sort_order", nullable = false)
    private int lessonSortOrder;

    @ManyToOne
    @JoinColumn(name = "mission_id", nullable = false)
    private Missions mission;

    private void validateMissionTitle() {
        if (this.lessonTitle != null && this.lessonTitle.trim().isEmpty()) {
            log.info("Lesson Title already exists {}", lessonTitle.trim());
        } else {
            log.info("Lesson Title is null or empty");
        }
    }

    private void validateSortOrder() {
        if (this.lessonSortOrder < 0) {
            log.info("Sort order cannot be less than 0");
        }
    }

}
