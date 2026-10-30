package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.enums.billing.BillingInterval;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

// Piano di abbonamento. Righe immutabili: un nuovo prezzo è un nuovo piano,
// così ogni contratto conserva il prezzo con cui è stato concluso
@Entity
@Table(name = "subscription_plans", uniqueConstraints = {
        @UniqueConstraint(name = "uq_subscription_plans_code", columnNames = "code"),
        @UniqueConstraint(name = "uq_subscription_plans_provider_price", columnNames = "provider_price_id")
})
@Check(name = "chk_subscription_plans_max_students", constraints = "max_students BETWEEN 1 AND 2")
@Check(name = "chk_subscription_plans_price", constraints = "price_cents > 0")
@Check(name = "chk_subscription_plans_validity", constraints = "valid_to IS NULL OR valid_to > valid_from")
// TODO (billing, punto 5): inserire piani (individuale/famiglia, mensile/annuale) e sconto beta quando i prezzi sono decisi
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    // Es. premium_monthly, family_yearly
    @NotBlank
    @Column(length = 60, nullable = false)
    private String code;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private BillingInterval billingInterval;

    // 1 = individuale, 2 = famiglia (massimo 2 figli)
    @Min(1)
    @Max(2)
    @Builder.Default
    @Column(nullable = false)
    private short maxStudents = 1;

    // Prezzo finale pagato dall'utente (IVA inclusa quando applicabile)
    @Positive
    @Column(nullable = false)
    private int priceCents;

    @Builder.Default
    @Column(length = 3, nullable = false)
    private String currency = "EUR";

    // Price di Stripe (price_...): null finché il piano non è configurato su Stripe
    @Column(columnDefinition = "TEXT")
    private String providerPriceId;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @NotNull
    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime validFrom = OffsetDateTime.now();

    private OffsetDateTime validTo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
