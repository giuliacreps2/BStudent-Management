package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.billing.PaymentProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Chi paga, collegato al suo Customer su Stripe. Sempre un adulto (lo verifica il service):
// un minore non può concludere il contratto (artt. 2 e 1425 c.c.)
@Entity
@Table(name = "billing_customers", uniqueConstraints = {
        @UniqueConstraint(name = "uq_billing_customers_user", columnNames = "user_id"),
        @UniqueConstraint(name = "uq_billing_customers_provider", columnNames = {"provider", "provider_customer_id"})
})
public class BillingCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billingCustomerId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private PaymentProvider provider = PaymentProvider.stripe;

    // cus_... di Stripe
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String providerCustomerId;

    // Determina il trattamento IVA (OSS sopra la soglia UE)
    @Builder.Default
    @Column(length = 2, nullable = false)
    private String countryCode = "IT";

    // Dati di fatturazione: solo per chi chiede la fattura
    @ToString.Exclude
    @Column(columnDefinition = "TEXT")
    private String billingName;

    @ToString.Exclude
    @Column(columnDefinition = "TEXT")
    private String billingAddress;

    @ToString.Exclude
    @Column(length = 16)
    private String taxCode;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
