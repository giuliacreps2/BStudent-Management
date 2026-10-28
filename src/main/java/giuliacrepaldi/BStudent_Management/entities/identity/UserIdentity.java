package giuliacrepaldi.BStudent_Management.entities.identity;

import giuliacrepaldi.BStudent_Management.enums.identity.AuthProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Identità esterna (es. Google) collegata a un account: un account può avere sia password che Google
@Entity
@Table(name = "user_identities", uniqueConstraints = {
        // Un'identità esterna appartiene a un solo account
        @UniqueConstraint(name = "uq_user_identities_subject", columnNames = {"provider", "provider_subject"}),
        // Al massimo un'identità per provider; copre anche la ricerca per user_id
        @UniqueConstraint(name = "uq_user_identities_user", columnNames = {"user_id", "provider"})
})
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userIdentityId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private AuthProvider provider;

    // "sub" di Google: stabile anche se l'utente cambia email
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String providerSubject;

    // Email comunicata dal provider al momento del collegamento (solo storico)
    @Column(length = 320)
    private String emailAtLink;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
