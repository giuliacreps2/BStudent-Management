package giuliacrepaldi.BStudent_Management.entities.notebook;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "resource_bookmarks", indexes = {
        @Index(name = "idx_resource_bookmarks_resource", columnList = "resource_id")
})
// Segnalibro del Quaderno (v0.9, blueprint §7.4, §14): la Risorsa salvata è un collegamento, non un contenuto
// del Quaderno. Le FK sono verificate dal DB
public class ResourceBookmark {

    // Chiave composta: (student_id, resource_id)
    @EmbeddedId
    @Builder.Default
    private ResourceBookmarkId id = new ResourceBookmarkId();

    @Column(nullable = false, updatable = false)
    private OffsetDateTime savedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ResourceBookmarkId implements Serializable {
        @Column(name = "student_id")
        private Long studentId;
        @Column(name = "resource_id")
        private Long resourceId;
    }
}
