package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "missions", uniqueConstraints = {
        // DEFERRABLE nel DB: il riordino lo rimanda al COMMIT con SET CONSTRAINTS
        @UniqueConstraint(name = "missions_course_id_sort_order_idx", columnNames = {"course_id", "sort_order"})
})
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionId;

    @NotBlank
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String missionTitle;

    @NotBlank
    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String missionDescription;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int missionSortOrder;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ContentStatus status = ContentStatus.draft;

    private OffsetDateTime publishedAt;

    // Valorizzato se e solo se status = withdrawn (chk_missions_withdrawn)
    private OffsetDateTime withdrawnAt;

    // Null = eredita dal corso. Il livello del corso è solo il default: ammesse sia missioni free
    // in un corso premium sia missioni premium in un corso free
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private AccessTier accessTier;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
