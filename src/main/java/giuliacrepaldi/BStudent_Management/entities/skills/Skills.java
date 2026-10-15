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
public class Skills {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long skillId;

    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String codeSkill;

    @Column(name = "name", length = 150, nullable = false)
    private String skillName;

    @Column(name = "description", length = 500)
    private String skillDescription;

    @Column(name = "status", nullable = false)
    private StatusSkill statusSkill = StatusSkill.draft;


}
