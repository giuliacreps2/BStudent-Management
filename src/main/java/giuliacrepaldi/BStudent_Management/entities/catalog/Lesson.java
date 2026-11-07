package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "lessons", uniqueConstraints = {
        // DEFERRABLE nel DB: il riordino lo rimanda al COMMIT con SET CONSTRAINTS
        @UniqueConstraint(name = "lessons_mission_id_sort_order_idx", columnNames = {"mission_id", "sort_order"}),
        // Target della FK composta (lesson_id, mission_id) di mission_exercises: deve restare non deferrable
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

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ContentStatus status = ContentStatus.draft;

    private OffsetDateTime publishedAt;

    // Valorizzato se e solo se status = withdrawn (chk_lessons_withdrawn)
    private OffsetDateTime withdrawnAt;

    // Date editoriali (v0.7), impostate da Hibernate
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;
}
