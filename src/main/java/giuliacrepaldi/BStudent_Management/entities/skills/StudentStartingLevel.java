package giuliacrepaldi.BStudent_Management.entities.skills;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "student_starting_levels", indexes = {
        @Index(name = "idx_student_starting_levels_subject", columnList = "subject_id"),
        @Index(name = "idx_student_starting_levels_skill", columnList = "skill_id")
})
// Livello di partenza dato dal test di orientamento (v0.10, Blueprint Gamification §2.6): vale come punti
// di partenza pari alla soglia del livello, usati solo per il livello. Le FK sono verificate dal DB
public class StudentStartingLevel {

    // Chiave composta: (student_id, subject_id, skill_id)
    @EmbeddedId
    @Builder.Default
    private StudentStartingLevelId id = new StudentStartingLevelId();

    @Positive
    @Column(nullable = false)
    private int levelNumber;

    @Column(nullable = false)
    private OffsetDateTime setAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StudentStartingLevelId implements Serializable {
        @Column(name = "student_id")
        private Long studentId;
        @Column(name = "subject_id")
        private Long subjectId;
        @Column(name = "skill_id")
        private Long skillId;
    }
}
