package giuliacrepaldi.BStudent_Management.entities.resources;

import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.enums.resources.ResourceLinkOrigin;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "resource_links", uniqueConstraints = {
        // DEFERRABLE nel DB, come gli altri ordinamenti
        @UniqueConstraint(name = "resource_links_resource_id_sort_order_idx", columnNames = {"resource_id", "sort_order"})
}, indexes = {
        @Index(name = "idx_resource_links_target_resource", columnList = "target_resource_id"),
        @Index(name = "idx_resource_links_target_topic", columnList = "target_topic_id")
})
// "Approfondisci" della Risorsa: solo struttura, nessun service né lettura nell'MVP (blueprint §7.4).
// Esattamente una destinazione (chk_resource_links_target); gli articoli del Blog arriveranno con la v0.10
public class ResourceLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceLinkId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_resource_id")
    private Resource targetResource;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_topic_id")
    private Topic targetTopic;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ResourceLinkOrigin origin = ResourceLinkOrigin.manual;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
