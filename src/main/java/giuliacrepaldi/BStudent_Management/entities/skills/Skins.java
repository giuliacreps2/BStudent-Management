package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.enums.skills.StatusSkill;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
public class Skins {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long skinId;

    @Column(name = "code", length = 80, nullable = false, unique = true)
    private String skinCode;

    @Column(name = "name", length = 150, nullable = false)
    private String skinName;

    @Column(name = "description")
    private String skinDescriptionSkin;

    @Column(name = "status", length = 40, nullable = false)
    private StatusSkill statusSkin = StatusSkill.draft;

}
