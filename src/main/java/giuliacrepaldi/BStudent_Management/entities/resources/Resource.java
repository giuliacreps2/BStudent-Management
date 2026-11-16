package giuliacrepaldi.BStudent_Management.entities.resources;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.resources.ResourceCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "resources", uniqueConstraints = {
        @UniqueConstraint(name = "uq_resources_subject_slug", columnNames = {"subject_id", "slug"})
}, indexes = {
        @Index(name = "idx_resources_topic", columnList = "topic_id")
})
// Risorsa della Palestra (v0.9, blueprint §7.4): il "libro", visibile solo con l'account.
// TODO (v0.10): togliere la colonna summary_example, non mappata: il mini esempio di "In breve" ha due parti
// (termine e traduzione) e sta in content.summaryExample
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ResourceCategory category;

    // Indirizzo della pagina, unico nella materia
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String slug;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // Testo di "In breve": 2–4 righe
    @Column(columnDefinition = "TEXT")
    private String summaryText;

    // Identificativo Cloudinary
    @Column(columnDefinition = "TEXT")
    private String coverReference;

    // Tabella, approfondimenti a blocchi, esempi e mini esempio: forma controllata da ContentBlocks
    @NotNull
    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String content = "{}";

    // Argomento della Palestra per "Vai agli esercizi", della stessa materia
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private AccessTier accessTier = AccessTier.premium;

    // "Le risorse più usate": scelte dall'autrice finché non c'è l'uso reale; icona insieme alla casella
    @Builder.Default
    @Column(nullable = false)
    private boolean isMostUsed = false;

    @Column(columnDefinition = "TEXT")
    private String mostUsedIcon;

    @Builder.Default
    @Column(nullable = false)
    private boolean isFeatured = false;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ContentStatus status = ContentStatus.draft;

    private OffsetDateTime publishedAt;

    // Valorizzato se e solo se status = withdrawn (chk_resources_withdrawn)
    private OffsetDateTime withdrawnAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
