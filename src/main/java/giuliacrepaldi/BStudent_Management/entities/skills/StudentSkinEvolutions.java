package giuliacrepaldi.BStudent_Management.entities.skills;

import jakarta.persistence.*;
import lombok.*;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_skin_id")
    private Long studentSkinEvolutionsId;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime unlockedAt = OffsetDateTime.now();

    @ManyToOne
    @JoinColumn(name = "evolution_id", nullable = false)
    private SkinEvolutions skinEvolution;

    @ManyToOne
    @JoinColumn(name = "skin_id", nullable = false)
    private Skins skin;
}
