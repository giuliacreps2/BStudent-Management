package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
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
@Table(name = "missions", uniqueConstraints = {
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

    private OffsetDateTime withdrawnAt;

    // Null = eredita dal corso; permette es. le prime missioni free di un corso premium
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private AccessTier accessTier;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
