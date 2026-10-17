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
public class QuestionBankEntries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionBankEntryId;

    @Column(name = "internal_title", nullable = false, columnDefinition = "TEXT")
    private String internalQuestionTitle;

    private void validateInternalQuestionTitle() {
        if (this.internalQuestionTitle != null && !this.internalQuestionTitle.trim().isEmpty()) {
            log.info("Internal Question Title Validation already exists {}", internalQuestionTitle.trim());
        } else {
            log.info("Internal Question Title Validation Failed");
        }
    }
}
