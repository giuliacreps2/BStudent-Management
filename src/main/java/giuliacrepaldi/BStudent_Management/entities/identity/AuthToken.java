package giuliacrepaldi.BStudent_Management.entities.identity;

import giuliacrepaldi.BStudent_Management.enums.identity.TokenPurpose;
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

@Entity
@Table(name = "auth_tokens", indexes = {
        @Index(name = "idx_auth_tokens_user", columnList = "user_id"),
        @Index(name = "idx_auth_tokens_expiry", columnList = "expires_at")
})
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @ToString.Exclude
    @NotBlank
    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String tokenHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private TokenPurpose purpose;

    @NotNull
    @Column(nullable = false, updatable = false)
    private OffsetDateTime expiresAt;

    private OffsetDateTime consumedAt;
    private OffsetDateTime revokedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    private void validateExpiry() {
        if (expiresAt != null && !expiresAt.isAfter(OffsetDateTime.now())) {
            throw new IllegalStateException("La data di scadenza deve essere successiva alla data di creazione");
        }
    }
}
