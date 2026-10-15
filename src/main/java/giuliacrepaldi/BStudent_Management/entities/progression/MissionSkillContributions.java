package giuliacrepaldi.BStudent_Management.entities.progression;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempts;
import giuliacrepaldi.BStudent_Management.entities.catalog.Missions;
import giuliacrepaldi.BStudent_Management.entities.identity.Students;
import giuliacrepaldi.BStudent_Management.entities.skills.Skills;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(

        //TODO da controllare soprattutto il pk
        // Indexes {
        //    (student_id, mission_id, skill_id) [pk]
        //  }
        indexes = {
                @Index(name = "student_id", columnList = "mission_id"),
                @Index(name = "student_id", columnList = "skill_id")
        }
)
public class MissionSkillContributions {

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal points;

    @Column(precision = 5, scale = 4, nullable = false)
    private BigDecimal factorSnapshot;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Students student;

    @ManyToOne
    @JoinColumn(name = "mission_id", nullable = false)
    private Missions mission;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skills skill;

    @ManyToMany
    @JoinColumn(name = "mission_attempt_id", nullable = false)
    private List<MissionAttempts> missionAttempt;

    private void validatePoints() {
        if (points.compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalArgumentException("Invalid points");
        }
    }


}
