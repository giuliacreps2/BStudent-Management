package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
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
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectId;

    @NotBlank
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String subjectName;

    // Fallback dei pesi skill degli esercizi di missione (100%); null = pesi sempre espliciti
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_skill_id")
    private Skill defaultSkill;
}
