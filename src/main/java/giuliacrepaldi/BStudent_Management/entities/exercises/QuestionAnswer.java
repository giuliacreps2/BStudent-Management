package giuliacrepaldi.BStudent_Management.entities.exercises;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "question_answers", uniqueConstraints = {
        @UniqueConstraint(name = "question_answers_question_id_sort_order_idx", columnNames = {"question_id", "sort_order"})
})
public class QuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionAnswerId;

    @NotBlank
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String answerText;

    @Column(nullable = false)
    private boolean isCorrect;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int answerSortOrder;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}
