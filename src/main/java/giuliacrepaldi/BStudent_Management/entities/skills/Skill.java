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
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skillId;

    @NotBlank
    @Column(name = "code", length = 60, nullable = false, unique = true)
    private String codeSkill;

    @NotBlank
    @Column(name = "name", length = 150, nullable = false)
    private String skillName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String skillDescription;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40, nullable = false)
    private StatusSkill statusSkill = StatusSkill.draft;
}
