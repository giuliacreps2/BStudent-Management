package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.Check;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Passo della sequenza della missione (v0.8): una lezione OPPURE un esercizio.
// Le regole sulla sequenza (inizia con una lezione, niente due esercizi di seguito) coinvolgono
// più righe: le garantisce MissionsService
@Entity
@Table(name = "mission_steps", uniqueConstraints = {
        // DEFERRABLE nel DB: il riordino lo rimanda al COMMIT con SET CONSTRAINTS
        @UniqueConstraint(name = "mission_steps_mission_id_sort_order_idx", columnNames = {"mission_id", "sort_order"}),
        @UniqueConstraint(name = "uq_mission_steps_lesson", columnNames = {"lesson_id"}),
        @UniqueConstraint(name = "uq_mission_steps_exercise", columnNames = {"mission_exercise_id"})
})
@Check(name = "chk_mission_steps_kind", constraints = "num_nonnulls(lesson_id, mission_exercise_id) = 1")
public class MissionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionStepId;

    // Ridondante rispetto a lesson.mission e missionExercise.mission: richiesto dalle FK composte
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int stepSortOrder;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_exercise_id")
    private MissionExercise missionExercise;

    public static MissionStep of(Lesson lesson, int sortOrder) {
        return MissionStep.builder().mission(lesson.getMission()).lesson(lesson).stepSortOrder(sortOrder).build();
    }

    public static MissionStep of(MissionExercise exercise, int sortOrder) {
        return MissionStep.builder().mission(exercise.getMission()).missionExercise(exercise).stepSortOrder(sortOrder).build();
    }

    public boolean isExercise() {
        return missionExercise != null;
    }
}
