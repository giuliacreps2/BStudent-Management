package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.enums.billing.RefundReason;
import giuliacrepaldi.BStudent_Management.enums.billing.RefundStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Rimborso, totale o parziale, di un pagamento
@Entity
@Table(name = "refunds", uniqueConstraints = {
        @UniqueConstraint(name = "uq_refunds_provider", columnNames = "provider_refund_id")
}, indexes = {
        @Index(name = "idx_refunds_payment", columnList = "payment_id")
})
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Positive
    @Column(nullable = false)
    private int amountCents;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private RefundReason reason;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private RefundStatus status = RefundStatus.pending;

    // re_... di Stripe
    @Column(columnDefinition = "TEXT")
    private String providerRefundId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime processedAt;
}
