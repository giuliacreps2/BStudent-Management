package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Esercizio della missione, non di una lezione (v0.8): la posizione è quella del suo passo (MissionStep)
@Entity
@Table(
        name = "mission_exercises",
        uniqueConstraints = {
                @UniqueConstraint(name = "mission_exercises_mission_id_question_bank_entry_id_idx",
                        columnNames = {"mission_id", "question_bank_entry_id"}),
                // Target della FK composta (mission_exercise_id, question_bank_entry_id) di exercise_attempts
                @UniqueConstraint(name = "uq_mission_exercises_id_entry",
                        columnNames = {"mission_exercise_id", "question_bank_entry_id"}),
                // Target della FK composta (mission_exercise_id, mission_id) di mission_steps
                @UniqueConstraint(name = "uq_mission_exercises_id_mission",
                        columnNames = {"mission_exercise_id", "mission_id"})
        },
        indexes = {
                @Index(name = "idx_mission_exercises_entry", columnList = "question_bank_entry_id")
        }
)
@Check(name = "chk_mission_exercises_allowed_duration", constraints = "allowed_duration_ms > 0")
@Check(name = "chk_mission_exercises_max_points", constraints = "max_points > 0")
@Check(name = "chk_mission_exercises_status", constraints = "status IN ('draft', 'published')")
public class MissionExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionExerciseId;

    // Solo draft o published. Nasce draft: in una missione pubblicata resta nascosto finché l'Admin
    // non lo pubblica; altrimenti viene pubblicato insieme alla missione
    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ContentStatus status = ContentStatus.draft;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_entry_id", nullable = false)
    private QuestionBankEntry questionBankEntry;

    // Valori sempre espliciti: alla creazione il service applica i default di academy_settings.
    // Il tempo parte quando l'esercizio compare allo studente (Blocco 5)
    @Positive
    @Column(nullable = false)
    private long allowedDurationMs;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal maxPoints;
}
