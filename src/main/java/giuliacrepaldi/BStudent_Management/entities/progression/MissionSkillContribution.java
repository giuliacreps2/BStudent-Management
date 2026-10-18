package giuliacrepaldi.BStudent_Management.entities.progression;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "mission_skill_contributions", indexes = {
        @Index(name = "idx_mission_skill_contrib_attempt", columnList = "mission_attempt_id"),
        @Index(name = "idx_mission_skill_contrib_mission", columnList = "mission_id"),
        @Index(name = "idx_mission_skill_contrib_skill", columnList = "skill_id")
})
public class MissionSkillContribution {

    // Chiave composta: (student_id, mission_id, skill_id)
    @EmbeddedId
    @Builder.Default
    private MissionSkillContributionId id = new MissionSkillContributionId();

    @NotNull
    @DecimalMin("0.00")
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal points;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("1")
    @Column(precision = 5, scale = 4, nullable = false)
    private BigDecimal factorSnapshot;

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ToString.Exclude
    @MapsId("missionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @ToString.Exclude
    @MapsId("skillId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    // Tentativo che ha prodotto il contributo
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_attempt_id", nullable = false)
    private MissionAttempt missionAttempt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MissionSkillContributionId implements Serializable {
        private Long studentId;
        private Long missionId;
        private Long skillId;
    }
}
