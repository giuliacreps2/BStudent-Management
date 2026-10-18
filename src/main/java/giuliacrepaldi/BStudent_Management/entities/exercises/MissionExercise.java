package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(
        name = "mission_exercises",
        uniqueConstraints = {
                @UniqueConstraint(name = "mission_exercises_mission_id_question_bank_entry_id_idx",
                        columnNames = {"mission_id", "question_bank_entry_id"}),
                // Target della FK composta (mission_exercise_id, question_bank_entry_id) di exercise_attempts
                @UniqueConstraint(name = "uq_mission_exercises_id_entry",
                        columnNames = {"mission_exercise_id", "question_bank_entry_id"})
        },
        indexes = {
                @Index(name = "idx_mission_exercises_entry", columnList = "question_bank_entry_id")
        }
)
public class MissionExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionExerciseId;

    // Ridondante rispetto a lesson.mission: richiesto dalla FK composta (lesson_id, mission_id)
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false, unique = true)
    private Lesson lesson;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_entry_id", nullable = false)
    private QuestionBankEntry questionBankEntry;

    @PrePersist
    @PreUpdate
    private void validateLessonBelongsToMission() {
        if (lesson == null || mission == null) {
            return;
        }
        if (!Objects.equals(lesson.getMission().getMissionId(), mission.getMissionId())) {
            throw new IllegalStateException("The lesson must belong to the mission of the exercise");
        }
    }
}
