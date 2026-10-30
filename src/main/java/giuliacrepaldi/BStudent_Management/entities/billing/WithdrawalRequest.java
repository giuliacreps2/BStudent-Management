package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Recesso entro 14 giorni, esercitato con la funzione "recedi dal contratto qui"
// (art. 54-bis Codice del consumo). Diverso dalla disdetta, che ferma solo i rinnovi
@Entity
@Table(name = "withdrawal_requests", uniqueConstraints = {
        @UniqueConstraint(name = "uq_withdrawal_requests_refund", columnNames = "refund_id")
})
// Unique parziali uq_withdrawal_requests_subscription/_product_order (un recesso per contratto): solo lato DB
@Check(name = "chk_withdrawal_requests_owner", constraints = "(subscription_id IS NULL) <> (product_order_id IS NULL)")
@Check(name = "chk_withdrawal_requests_retained", constraints = "retained_amount_cents >= 0")
// TODO (billing, punto 2): flusso del pulsante di recesso, vedi services/billing/package-info.java
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long withdrawalRequestId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_order_id")
    private ProductOrder productOrder;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @NotNull
    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime requestedAt = OffsetDateTime.now();

    // Conferma del recesso su supporto durevole (email)
    private OffsetDateTime confirmationSentAt;

    // Parte trattenuta per il servizio già fruito (art. 57 c. 3)
    @PositiveOrZero
    @Column(nullable = false)
    private int retainedAmountCents;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id")
    private Refund refund;
}
