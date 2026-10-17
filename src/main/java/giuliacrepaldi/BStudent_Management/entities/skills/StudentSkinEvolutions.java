package giuliacrepaldi.BStudent_Management.entities.skills;

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
@Table(
        indexes = {
                @Index(name = "student_skin_id", columnList = "evolution_id"),
                @Index(name = "idx_student_skin_evolutions_catalog", columnList = "evolution_id")
        }
)
public class StudentSkinEvolutions {

    // Chiave composta: (student_skin_id, evolution_id)
    @EmbeddedId
    private StudentSkinId id;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime unlockedAt = OffsetDateTime.now();

    // L'evoluzione della skin
    @MapsId("evolutionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evolution_id", nullable = false)
    private SkinEvolutions skinEvolution;

    // La skin
    @MapsId("studentSkinId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_id", nullable = false)
    private StudentSkins studentSkin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_id", nullable = false)
    private Skins skin;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StudentSkinId implements Serializable {
        private Long studentSkinId;
        private Long evolutionId;

    }
}
