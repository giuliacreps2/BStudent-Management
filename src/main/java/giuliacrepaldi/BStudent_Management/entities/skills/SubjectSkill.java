package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "subject_skills", uniqueConstraints = {
        // DEFERRABLE nel DB: il riordino lo rimanda al COMMIT con SET CONSTRAINTS
        @UniqueConstraint(name = "subject_skills_subject_id_sort_order_idx", columnNames = {"subject_id", "sort_order"})
}, indexes = {
        @Index(name = "idx_subject_skills_skill", columnList = "skill_id")
})
// Skill scelte dalla materia, nell'ordine dell'autrice (v0.9, blueprint §10.4-ter)
public class SubjectSkill {

    // Chiave composta: (subject_id, skill_id)
    @EmbeddedId
    @Builder.Default
    private SubjectSkillId id = new SubjectSkillId();

    @ToString.Exclude
    @MapsId("subjectId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ToString.Exclude
    @MapsId("skillId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class SubjectSkillId implements Serializable {
        private Long subjectId;
        private Long skillId;
    }
}
