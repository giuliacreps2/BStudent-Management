package giuliacrepaldi.BStudent_Management.entities.identity;

import giuliacrepaldi.BStudent_Management.enums.identity.Relationship;
import giuliacrepaldi.BStudent_Management.enums.identity.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(indexes = {
        @Index(name = "idx_guardian_links_student", columnList = "student_id")
})
public class GuardianLinks {

    // Chiave composta: (guardian_user_id, student_id)
    @EmbeddedId
    private GuardianLinkId id;

    // Il genitore: riempie guardian_user_id della chiave
    @MapsId("guardianUserId")
    @ManyToOne
    @JoinColumn(name = "guardian_user_id")
    private Users guardian;

    // Lo studente: riempie student_id della chiave
    @MapsId("studentId")
    @ManyToOne
    @JoinColumn(name = "student_id")
    private Students student;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Relationship relationship;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.pending;

    private OffsetDateTime verifiedAt;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

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
