package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
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

    // Titolo per lo studente (v0.9, blueprint §6.1): sulla scheda, non sulla versione.
    // Può mancare solo in bozza (chk_question_bank_entries_title)
    @Column(columnDefinition = "TEXT")
    private String title;

    // Argomento facoltativo: collega esercizio -> argomento -> teoria (blueprint §10.5)
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

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
