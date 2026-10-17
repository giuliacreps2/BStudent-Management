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
        @Index(name = "uestion_bank_entry_id", columnList = "version_number", unique = true)
})
public class QuestionVersions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionVersionId;

    @Column(name = "version_number", nullable = false)
    private int questionVersionNumber;

    @ManyToOne
    @JoinColumn(name = "question_bank_entry_id", nullable = false)
    private QuestionBankEntries questionBankEntries;


    private void validateQuestionVersionNumber() {
        if (this.questionVersionNumber < 0) {
            log.info("Question Version Number cannot be negative");
        }
    }
}
