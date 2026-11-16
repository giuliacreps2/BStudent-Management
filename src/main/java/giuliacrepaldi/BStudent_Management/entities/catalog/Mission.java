package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "missions", uniqueConstraints = {
        // DEFERRABLE nel DB: il riordino lo rimanda al COMMIT con SET CONSTRAINTS
        @UniqueConstraint(name = "missions_course_id_sort_order_idx", columnNames = {"course_id", "sort_order"}),
        @UniqueConstraint(name = "uq_missions_course_slug", columnNames = {"course_id", "slug"})
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

    // Indirizzo della pagina, unico nel corso (v0.9): l'ordine cambia con il riordino
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String coverReference;

    // Prova finale: al massimo una per corso (uq_missions_final_per_course), non per forza l'ultima
    @Builder.Default
    @Column(nullable = false)
    private boolean isFinal = false;

    // Video facoltativo del briefing: entrambi o nessuno (chk_missions_briefing_video)
    @Column(columnDefinition = "TEXT")
    private String briefingVideoProvider;

    @Column(columnDefinition = "TEXT")
    private String briefingVideoIdentifier;

    // Battute della guida nel briefing, da 1 a 5 (limite nel service). Ordine da 1 come gli altri sort_order
    @ToString.Exclude
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "mission_briefing_lines", joinColumns = @JoinColumn(name = "mission_id"))
    @OrderColumn(name = "sort_order")
    @ListIndexBase(1)
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private List<String> briefingLines = new ArrayList<>();

    // "Cosa imparerai": al massimo 5 frasi brevi (limite nel service)
    @ToString.Exclude
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "mission_learning_goals", joinColumns = @JoinColumn(name = "mission_id"))
    @OrderColumn(name = "sort_order")
    @ListIndexBase(1)
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private List<String> learningGoals = new ArrayList<>();

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ContentStatus status = ContentStatus.draft;

    private OffsetDateTime publishedAt;

    // Valorizzato se e solo se status = withdrawn (chk_missions_withdrawn)
    private OffsetDateTime withdrawnAt;

    // Date editoriali (v0.7), impostate da Hibernate
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

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
