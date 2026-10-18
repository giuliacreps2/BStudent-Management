package giuliacrepaldi.BStudent_Management.entities.skills;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "skin_evolutions", uniqueConstraints = {
        @UniqueConstraint(name = "skin_evolutions_skin_id_stage_number_idx", columnNames = {"skin_id", "stage_number"}),
        // Target della FK composta (skin_id, evolution_id) di student_skin_evolutions
        @UniqueConstraint(name = "skin_evolutions_skin_id_evolution_id_idx", columnNames = {"skin_id", "evolution_id"})
})
public class SkinEvolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long evolutionId;

    @PositiveOrZero
    @Column(nullable = false)
    private int stageNumber;

    @NotBlank
    @Column(name = "name", length = 150, nullable = false)
    private String skinName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String skinDescription;

    @NotBlank
    @Column(name = "asset_path", nullable = false, columnDefinition = "TEXT")
    private String skinAssetPath;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_id", nullable = false)
    private Skin skin;
}
