package giuliacrepaldi.BStudent_Management.entities.exercises;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.Check;
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
@Table(name = "question_versions", uniqueConstraints = {
        @UniqueConstraint(name = "question_versions_question_bank_entry_id_version_number_idx",
                columnNames = {"question_bank_entry_id", "version_number"}),
        // Target della FK composta (question_version_id, question_bank_entry_id) di exercise_attempts
        @UniqueConstraint(name = "uq_question_versions_id_entry",
                columnNames = {"question_version_id", "question_bank_entry_id"})
})
@Check(name = "chk_question_versions_published", constraints = "NOT is_published OR published_at IS NOT NULL")
@Check(name = "chk_question_versions_replaced",
        constraints = "replaced_at IS NULL OR (NOT is_published AND published_at IS NOT NULL)")
// Indice unico parziale gestito solo dal DB:
// uq_question_versions_published (question_bank_entry_id) WHERE is_published
public class QuestionVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionVersionId;

    @Positive
    @Column(name = "version_number", nullable = false)
    private int questionVersionNumber;

    // Una sola versione pubblicata per esercizio: è l'unica che lo studente svolge.
    // Per cambiarla si toglie prima il flag alla vecchia, poi lo si mette alla nuova
    @Builder.Default
    @Column(nullable = false)
    private boolean isPublished = false;

    // Ultima pubblicazione: con replacedAt dà il periodo "in uso dal ... al ..."
    private OffsetDateTime publishedAt;

    // Quando ha smesso di essere la versione pubblicata (chk_question_versions_replaced)
    private OffsetDateTime replacedAt;

    // Date editoriali (v0.7), impostate da Hibernate. updatedAt va toccato a mano quando cambiano
    // domanda, risposte o rubriche: stanno in altre tabelle e non rendono "sporca" la versione
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_entry_id", nullable = false)
    private QuestionBankEntry questionBankEntry;
}
