package giuliacrepaldi.BStudent_Management.entities.exercises;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Slf4j

@Entity
@Table(indexes = {
        @Index(name = "question_id", columnList = "sort_order", unique = true)
})
public class QuestionAnswers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long questionAnswerId;

    @Column(name = "text", nullable = false)
    private String questionText;

    @Column(nullable = false)
    private boolean isCorrect;

    @Column(name = "sort_order", nullable = false)
    private int questionSortOrder;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Questions question;

    private void validateQuestionSortOrder() {
        if (this.questionSortOrder < 0) {
            log.info("Question Sort order cannot be negative");
        }
    }
}
