package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ContentStatus status = ContentStatus.draft;

    private OffsetDateTime publishedAt;

    // Valorizzato se e solo se status = withdrawn (chk_question_bank_entries_withdrawn)
    private OffsetDateTime withdrawnAt;

    // Date editoriali (v0.7), impostate da Hibernate
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
