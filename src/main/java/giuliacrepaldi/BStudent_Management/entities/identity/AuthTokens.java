package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(indexes = {
        @Index(name = "idx_auth_tokens_user", columnList = "user_id"),
        @Index(name = "idx_auth_tokens_expiry", columnList = "expires_at")
})
public class AuthTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long tokenId;

    @Column(unique = true, nullable = false)
    private String tokenHash;

    @Column(length = 40, nullable = false)
    private String purpose;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    private OffsetDateTime consumedAt;
    private OffsetDateTime revokedAt;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @PrePersist
    @PreUpdate
    public void validateDates() {
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("La data di scadenza deve essere successiva alla data di creazione");
        }
    }
}
