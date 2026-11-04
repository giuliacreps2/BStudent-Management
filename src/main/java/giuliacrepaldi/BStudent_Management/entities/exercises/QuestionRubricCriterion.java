package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.enums.exercises.RubricCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "question_rubric_criteria", uniqueConstraints = {
        // DEFERRABLE nel DB: il riordino lo rimanda al COMMIT con SET CONSTRAINTS
        @UniqueConstraint(name = "question_rubric_criteria_question_id_sort_order_idx",
                columnNames = {"question_id", "sort_order"})
})
// Guida per la docente nella v0.1: nessun punteggio per criterio.
// Ammessi solo sulle traduzioni di versione: lo verifica il service
public class QuestionRubricCriterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionRubricCriterionId;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int criterionSortOrder;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private RubricCategory category;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
