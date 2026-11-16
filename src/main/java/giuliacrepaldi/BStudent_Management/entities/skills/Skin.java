package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.enums.skills.StatusSkill;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "skins")
public class Skin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skinId;

    @NotBlank
    @Column(name = "code", length = 80, nullable = false, unique = true)
    private String skinCode;

    @NotBlank
    @Column(name = "name", length = 150, nullable = false)
    private String skinName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String skinDescription;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40, nullable = false)
    private StatusSkill statusSkin = StatusSkill.draft;

    // Personaggio iniziale: si sceglie alla registrazione e si cambia liberamente. Gli altri pubblicati
    // compaiono come "Prossimamente" (v0.10, Blueprint Gamification §5)
    @Builder.Default
    @Column(name = "is_starter", nullable = false)
    private boolean starter = false;

    // Motto facoltativo mostrato nella pagina del personaggio
    @Column(columnDefinition = "TEXT")
    private String motto;
}
