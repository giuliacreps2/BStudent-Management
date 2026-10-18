package giuliacrepaldi.BStudent_Management.entities.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "lessons", uniqueConstraints = {
        @UniqueConstraint(name = "lessons_mission_id_sort_order_idx", columnNames = {"mission_id", "sort_order"}),
        // Target della FK composta (lesson_id, mission_id) di mission_exercises
        @UniqueConstraint(name = "lessons_lesson_id_mission_id_idx", columnNames = {"lesson_id", "mission_id"})
})
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lessonId;

    @NotBlank
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String lessonTitle;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int lessonSortOrder;

    private OffsetDateTime withdrawnAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;
}
