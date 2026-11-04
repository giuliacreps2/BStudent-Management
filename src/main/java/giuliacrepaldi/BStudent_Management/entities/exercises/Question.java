package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.exercises.KindQuestion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "questions")
// Il vincolo sui valori di "kind" e' generato automaticamente da Hibernate per gli enum STRING
@Check(
        name = "chk_questions_fields_by_kind",
        constraints = """
                (
                    kind IN ('sentence_translation', 'passage_translation')
                    AND source_text IS NOT NULL
                    AND general_explanation IS NOT NULL
                )
                OR (
                    kind IN ('multiple_choice', 'fill_in')
                    AND source_text IS NULL
                )
                """
)
// Scelta multipla e completamento automatici, traduzione di versione docente,
// traduzione di frase a scelta dell'autrice
@Check(
        name = "chk_questions_assessment_mode",
        constraints = """
                (kind IN ('multiple_choice', 'fill_in') AND assessment_mode = 'automatic')
                OR (kind = 'passage_translation' AND assessment_mode = 'teacher')
                OR (kind = 'sentence_translation' AND assessment_mode IN ('automatic', 'teacher'))
                """
)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String sourceText;

    @Column(columnDefinition = "TEXT")
    private String generalExplanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, columnDefinition = "TEXT")
    private KindQuestion kindQuestion;

    // Sulla domanda, quindi versionata con il contenuto
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private AssessmentMethod assessmentMode;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_version_id", nullable = false, unique = true)
    private QuestionVersion questionVersion;
}
