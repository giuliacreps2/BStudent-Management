package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.billing.PaymentKind;
import giuliacrepaldi.BStudent_Management.enums.billing.PaymentStatus;
import giuliacrepaldi.BStudent_Management.enums.billing.TaxTreatment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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

// Un addebito: il primo, ogni rinnovo, i conguagli, gli ordini singoli.
// Conserva il trattamento fiscale con cui è stato incassato (oggi forfettario, IVA 0)
@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_payments_provider_invoice", columnNames = "provider_invoice_id"),
        @UniqueConstraint(name = "uq_payments_provider_intent", columnNames = "provider_payment_intent_id")
}, indexes = {
        @Index(name = "idx_payments_subscription", columnList = "subscription_id"),
        @Index(name = "idx_payments_product_order", columnList = "product_order_id"),
        @Index(name = "idx_payments_payer", columnList = "payer_user_id")
})
// Ogni pagamento appartiene a un abbonamento oppure a un ordine singolo
@Check(name = "chk_payments_owner", constraints = "(subscription_id IS NULL) <> (product_order_id IS NULL)")
@Check(name = "chk_payments_amounts", constraints = """
        amount_cents >= 0 AND discount_cents >= 0 AND tax_cents >= 0 AND tax_cents <= amount_cents""")
@Check(name = "chk_payments_tax_rate", constraints = "tax_rate_bps BETWEEN 0 AND 10000")
@Check(name = "chk_payments_period", constraints = "period_end IS NULL OR period_start IS NULL OR period_end > period_start")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_user_id", nullable = false)
    private User payer;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_order_id")
    private ProductOrder productOrder;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private PaymentKind kind;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private PaymentStatus status = PaymentStatus.pending;

    // Importo addebitato, già al netto dello sconto
    @PositiveOrZero
    @Column(nullable = false)
    private int amountCents;

    @PositiveOrZero
    @Column(nullable = false)
    private int discountCents;

    // Imposta compresa in amountCents; 0 nel regime forfettario
    @PositiveOrZero
    @Column(nullable = false)
    private int taxCents;

    // Aliquota in punti base (2200 = 22%)
    @PositiveOrZero
    @Column(nullable = false)
    private int taxRateBps;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private TaxTreatment taxTreatment = TaxTreatment.forfettario;

    @Builder.Default
    @Column(length = 2, nullable = false)
    private String customerCountry = "IT";

    @Builder.Default
    @Column(length = 3, nullable = false)
    private String currency = "EUR";

    // Periodo coperto (solo per gli abbonamenti)
    private OffsetDateTime periodStart;

    private OffsetDateTime periodEnd;

    private OffsetDateTime paidAt;

    // Codice di errore di Stripe (es. card_declined)
    @Column(columnDefinition = "TEXT")
    private String failureCode;

    // in_... e pi_... di Stripe
    @Column(columnDefinition = "TEXT")
    private String providerInvoiceId;

    @Column(columnDefinition = "TEXT")
    private String providerPaymentIntentId;

    // Fattura emessa su richiesta con il software del commercialista
    @Column(length = 40)
    private String invoiceNumber;

    private OffsetDateTime invoiceIssuedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
