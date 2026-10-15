package giuliacrepaldi.BStudent_Management.entities.skills;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(indexes = {
        @Index(name = "skin_id", columnList = "stage_number", unique = true),
        @Index(name = "skin_id", columnList = "evolution_id", unique = true)
}
)
public class SkinEvolutions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long evolutionId;

    @Column(nullable = false)
    private int stageNumber;

    @Column(name = "name", length = 150, nullable = false)
    private String skinName;

    @Column(name = "description", length = 500)
    private String skinDescription;

    @Column(name = "asset_path", length = 500, nullable = false)
    private String skinAssetPath;
    
    @ManyToOne
    @JoinColumn(nullable = false)
    private Skins skin;

    public void validateStageNumber() {
        if (this.stageNumber >= 0) {
            throw new IllegalArgumentException("Stage number must be greater than or equal to 0");
        }
    }
}
