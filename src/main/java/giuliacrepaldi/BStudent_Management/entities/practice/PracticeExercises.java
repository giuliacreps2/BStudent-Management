package giuliacrepaldi.BStudent_Management.entities.practice;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntries;
import giuliacrepaldi.BStudent_Management.entities.skills.Skills;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Slf4j

@Entity
public class PracticeExercises {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long practiceExerciseId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal naxPoints;

    @ManyToOne
    @JoinColumn(name = "question_bank_entry_id", nullable = false, unique = true)
    private QuestionBankEntries questionBankEntrie;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skills skill;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topics topic;

    private void validateMaxPoints(BigDecimal maxPoints) {
        if (maxPoints.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("The maximum allowed points can not be negative");
        }
    }
}
