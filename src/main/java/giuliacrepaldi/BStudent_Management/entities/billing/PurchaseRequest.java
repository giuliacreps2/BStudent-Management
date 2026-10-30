package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.billing.PurchaseRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Lo studente minorenne chiede un piano o un prodotto a un genitore/tutore, che decide e paga.
// Una sola richiesta aperta per studente: niente insistenza (art. 26 lett. e Codice del consumo)
@Entity
@Table(name = "purchase_requests", uniqueConstraints = {
        @UniqueConstraint(name = "uq_purchase_requests_token", columnNames = "token_hash")
}, indexes = {
        @Index(name = "idx_purchase_requests_expiry", columnList = "expires_at")
})
// Unique parziale uq_open_purchase_requests_student (status = 'pending'): solo lato DB
@Check(name = "chk_purchase_requests_item", constraints = "(plan_id IS NULL) <> (product_id IS NULL)")
@Check(name = "chk_purchase_requests_expiry", constraints = "expires_at > created_at")
public class PurchaseRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseRequestId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotBlank
    @Column(length = 320, nullable = false)
    private String inviteeEmailNormalized;

    // Valorizzato quando il genitore/tutore apre il link con il suo account
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_user_id")
    private User guardian;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private PurchaseRequestStatus status = PurchaseRequestStatus.pending;

    // Solo l'hash SHA-256: il token in chiaro esiste solo nell'email
    @ToString.Exclude
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String tokenHash;

    @NotNull
    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime resolvedAt;
}
