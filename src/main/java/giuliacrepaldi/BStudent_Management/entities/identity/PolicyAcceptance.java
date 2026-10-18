package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "policy_acceptances", uniqueConstraints = {
        // Un utente accetta ogni versione di un documento una sola volta
        @UniqueConstraint(name = "policy_acceptances_user_id_document_code_document_version_idx",
                columnNames = {"user_id", "document_code", "document_version"})
})
public class PolicyAcceptance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long acceptanceId;

    @NotBlank
    @Column(length = 80, nullable = false)
    private String documentCode;

    @NotBlank
    @Column(length = 40, nullable = false)
    private String documentVersion;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime acknowledgedAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
