package giuliacrepaldi.BStudent_Management.entities.attempts;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "mission_checkpoints", indexes = {
        @Index(name = "idx_mission_checkpoints_item", columnList = "mission_exercise_id"),
        @Index(name = "idx_mission_checkpoints_attempt", columnList = "mission_attempt_id")
})
// Solo esercizi superati: il DB verifica con FK composte che giudizio, tentativo,
// percorso ed esercizio siano coerenti tra loro
@Check(name = "chk_mission_checkpoints_passed", constraints = "passed")
public class MissionCheckpoint {

    // Chiave composta: (mission_run_id, mission_exercise_id)
    @EmbeddedId
    @Builder.Default
    private MissionCheckpointId id = new MissionCheckpointId();

    @ToString.Exclude
    @MapsId("missionRunId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_run_id")
    private MissionRun missionRun;

    @ToString.Exclude
    @MapsId("missionExerciseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_exercise_id")
    private MissionExercise missionExercise;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_attempt_id", nullable = false, unique = true)
    private ExerciseAssessment exerciseAssessment;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_attempt_id", nullable = false)
    private MissionAttempt missionAttempt;

    // Sempre true: esiste solo per la FK composta verso exercise_assessments (exercise_attempt_id, passed)
    @Builder.Default
    @Column(nullable = false)
    private boolean passed = true;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MissionCheckpointId implements Serializable {
        private Long missionRunId;
        private Long missionExerciseId;
    }
}
