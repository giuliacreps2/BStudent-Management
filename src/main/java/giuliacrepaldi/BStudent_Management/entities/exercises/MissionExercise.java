package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
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
@Check(name = "chk_mission_exercises_allowed_duration", constraints = "allowed_duration_ms > 0")
@Check(name = "chk_mission_exercises_max_points", constraints = "max_points > 0")
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

    // Valori sempre espliciti: alla creazione il service applica i default di academy_settings
    @Positive
    @Column(nullable = false)
    private long allowedDurationMs;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal maxPoints;

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
