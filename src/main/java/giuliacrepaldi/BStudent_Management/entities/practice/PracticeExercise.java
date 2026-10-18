package giuliacrepaldi.BStudent_Management.entities.practice;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "practice_exercises", indexes = {
        @Index(name = "idx_practice_exercises_skill", columnList = "skill_id"),
        @Index(name = "idx_practice_exercises_topic", columnList = "topic_id")
})
public class PracticeExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long practiceExerciseId;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal maxPoints;

    private OffsetDateTime withdrawnAt;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_entry_id", nullable = false, unique = true)
    private QuestionBankEntry questionBankEntry;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
}
