package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.enums.exercises.KindQuestion;
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
//@Check(
//        name = "chk_questions_kind",
//        constraints = """
//                kind IN (
//                    'multiple_choice',
//                    'fill_in',
//                    'sentence_translation',
//                    'passage_translation'
//                )
//                """
//)
//@Check(
//        name = "chk_questions_fields_by_kind",
//        constraints = """
//                (
//                    kind IN ('sentence_translation', 'passage_translation')
//                    AND source_text IS NOT NULL
//                    AND general_explanation IS NOT NULL
//                )
//                OR (
//                    kind IN ('multiple_choice', 'fill_in')
//                    AND source_text IS NULL
//                )
//                """
//)
public class Questions {

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
    @Column(name = "kind", nullable = false)
    private KindQuestion kindQuestion;

    @ManyToOne
    @JoinColumn(name = "question_version_id", nullable = false, unique = true)
    private QuestionVersions questionVersion;
}
