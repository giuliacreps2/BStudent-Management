package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "mission_exercise_skills", indexes = {
        @Index(name = "idx_mission_exercise_skills_skill", columnList = "skill_id")
})
// La somma dei pesi dell'esercizio = 100 coinvolge più righe: la garantisce il service.
// Nessuna riga = 100% sulla skill predefinita della materia (subjects.default_skill_id)
@Check(name = "chk_mission_exercise_skills_weight", constraints = "weight > 0 AND weight <= 100")
public class MissionExerciseSkill {

    // Chiave composta: (mission_exercise_id, skill_id)
    @EmbeddedId
    @Builder.Default
    private MissionExerciseSkillId id = new MissionExerciseSkillId();

    @ToString.Exclude
    @MapsId("missionExerciseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_exercise_id")
    private MissionExercise missionExercise;

    @ToString.Exclude
    @MapsId("skillId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    // Percentuale del risultato dell'esercizio attribuita alla skill
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @DecimalMax("100.00")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MissionExerciseSkillId implements Serializable {
        private Long missionExerciseId;
        private Long skillId;
    }
}
