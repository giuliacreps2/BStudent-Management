package giuliacrepaldi.BStudent_Management.entities.exercises;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "question_versions", uniqueConstraints = {
        @UniqueConstraint(name = "question_versions_question_bank_entry_id_version_number_idx",
                columnNames = {"question_bank_entry_id", "version_number"})
})
public class QuestionVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionVersionId;

    @Positive
    @Column(name = "version_number", nullable = false)
    private int questionVersionNumber;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_entry_id", nullable = false)
    private QuestionBankEntry questionBankEntry;
}
