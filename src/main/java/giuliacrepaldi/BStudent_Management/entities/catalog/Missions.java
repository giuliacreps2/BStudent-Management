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
        @Index(name = "course_id", columnList = "sort_order", unique = true)
})
public class Missions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long missionId;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String missionTitle;

    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String missionDescription;

    @Column(name = "sort_order", nullable = false)
    private int missionSortOrder;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Courses course;

    private void validateMissionTitle() {
        if (this.missionTitle != null && this.missionTitle.trim().isEmpty()) {
            log.info("Mission Title already exists {}", missionTitle.trim());
        } else {
            log.info("Mission Title is null or empty");
        }
    }

    private void validateSortOrder() {
        if (this.missionSortOrder < 0) {
            log.info("Sort order cannot be less than 0");
        }
    }

}
