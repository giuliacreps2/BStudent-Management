package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(indexes = {
        @Index(name = "idx_auth_tokens_user", columnList = "user_id"),
        @Index(name = "idx_auth_tokens_expiry", columnList = "expires_At")
})
public class AuthTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long authTokenId;

    @Column(unique = true, nullable = false)
    private String tokenHash;

    @Column(length = 40, nullable = false)
    private String purpose;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
    private LocalDateTime consumedAt;
    private LocalDateTime revokedAt;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "user_id")
    private Users userId;

    @PrePersist
    @PreUpdate
    public void validateDates() {
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("La data di scadenza deve essere successiva alla data di creazione");
        }
    }
}
