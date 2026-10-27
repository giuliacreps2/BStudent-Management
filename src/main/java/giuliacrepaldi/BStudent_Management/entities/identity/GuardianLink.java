package giuliacrepaldi.BStudent_Management.entities.identity;

import giuliacrepaldi.BStudent_Management.enums.identity.Relationship;
import giuliacrepaldi.BStudent_Management.enums.identity.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "guardian_links", indexes = {
        @Index(name = "idx_guardian_links_student", columnList = "student_id")
})
// Prova del consenso (GDPR art. 7.1): un legame verificato dice quando e su quale versione
@Check(name = "chk_guardian_links_consent",
        constraints = "verification_status <> 'verified' OR (verified_at IS NOT NULL AND consent_document_version IS NOT NULL)")
public class GuardianLink {

    // Chiave composta: (guardian_user_id, student_id)
    @EmbeddedId
    @Builder.Default
    private GuardianLinkId id = new GuardianLinkId();

    // Il genitore: riempie guardian_user_id della chiave
    @ToString.Exclude
    @MapsId("guardianUserId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_user_id")
    private User guardian;

    // Lo studente: riempie student_id della chiave
    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(length = 60, nullable = false)
    private Relationship relationship;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.pending;

    private OffsetDateTime verifiedAt;

    // Versione dell'informativa su cui il genitore/tutore ha dato il consenso
    @Column(length = 40)
    private String consentDocumentVersion;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class GuardianLinkId implements Serializable {
        private Long guardianUserId;
        private Long studentId;
    }
}
