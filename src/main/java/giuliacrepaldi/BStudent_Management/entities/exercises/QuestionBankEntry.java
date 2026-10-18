package giuliacrepaldi.BStudent_Management.entities.exercises;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "question_bank_entries")
public class QuestionBankEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionBankEntryId;

    @NotBlank
    @Column(name = "internal_title", nullable = false, columnDefinition = "TEXT")
    private String internalQuestionTitle;

    private OffsetDateTime withdrawnAt;
}
