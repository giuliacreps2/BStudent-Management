package giuliacrepaldi.BStudent_Management.entities.skills;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "student_skin_evolutions", indexes = {
        // student_skin_id e' gia' coperto dalla PK composta
        @Index(name = "idx_student_skin_evolutions_catalog", columnList = "skin_id, evolution_id")
})
public class StudentSkinEvolution {

    // Chiave composta: (student_skin_id, evolution_id)
    @EmbeddedId
    @Builder.Default
    private StudentSkinEvolutionId id = new StudentSkinEvolutionId();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime unlockedAt;

    @ToString.Exclude
    @MapsId("studentSkinId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_skin_id", nullable = false)
    private StudentSkin studentSkin;

    @ToString.Exclude
    @MapsId("evolutionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evolution_id", nullable = false)
    private SkinEvolution skinEvolution;

    // Ridondante: richiesto dalle FK composte verso student_skins e skin_evolutions
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_id", nullable = false)
    private Skin skin;

    @PrePersist
    @PreUpdate
    private void validateSameSkin() {
        if (skin == null || studentSkin == null || skinEvolution == null) {
            return;
        }
        Long skinId = skin.getSkinId();
        if (!Objects.equals(studentSkin.getSkin().getSkinId(), skinId)
                || !Objects.equals(skinEvolution.getSkin().getSkinId(), skinId)) {
            throw new IllegalStateException("Student skin and evolution must refer to the same skin");
        }
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StudentSkinEvolutionId implements Serializable {
        private Long studentSkinId;
        private Long evolutionId;
    }
}
