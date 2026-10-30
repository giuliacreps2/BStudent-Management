package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.billing.ProductOrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

// Acquisto singolo di un prodotto: lo paga un adulto, ne beneficia uno studente
@Entity
@Table(name = "product_orders", uniqueConstraints = {
        @UniqueConstraint(name = "uq_product_orders_checkout", columnNames = "provider_checkout_session_id")
}, indexes = {
        @Index(name = "idx_product_orders_payer", columnList = "payer_user_id"),
        @Index(name = "idx_product_orders_student", columnList = "student_id")
})
@Check(name = "chk_product_orders_amount", constraints = "amount_cents > 0")
public class ProductOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productOrderId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_user_id", nullable = false)
    private User payer;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ProductOrderStatus status = ProductOrderStatus.pending;

    // Prezzo del prodotto al momento dell'ordine
    @Positive
    @Column(nullable = false)
    private int amountCents;

    @Builder.Default
    @Column(length = 3, nullable = false)
    private String currency = "EUR";

    // cs_... di Stripe Checkout
    @Column(columnDefinition = "TEXT")
    private String providerCheckoutSessionId;

    private OffsetDateTime withdrawalDeadline;

    private OffsetDateTime immediateStartConsentAt;

    @NotBlank
    @Column(length = 40, nullable = false)
    private String termsVersion;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime paidAt;
}
