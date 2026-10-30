package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.enums.billing.DiscountAudience;
import giuliacrepaldi.BStudent_Management.enums.billing.DiscountDuration;
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

// Sconto applicato al checkout (es. riservato agli utenti beta), speculare a un coupon Stripe
@Entity
@Table(name = "discounts", uniqueConstraints = {
        @UniqueConstraint(name = "uq_discounts_code", columnNames = "code"),
        @UniqueConstraint(name = "uq_discounts_provider_coupon", columnNames = "provider_coupon_id")
})
// Esattamente uno tra percentuale e importo fisso
@Check(name = "chk_discounts_amount", constraints = """
        (percent_off IS NOT NULL AND percent_off BETWEEN 1 AND 100 AND amount_off_cents IS NULL)
        OR (amount_off_cents IS NOT NULL AND amount_off_cents > 0 AND percent_off IS NULL)""")
@Check(name = "chk_discounts_months", constraints = "(duration = 'repeating') = (duration_in_months IS NOT NULL)")
@Check(name = "chk_discounts_validity", constraints = "valid_until IS NULL OR valid_until > valid_from")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;

    @NotBlank
    @Column(length = 60, nullable = false)
    private String code;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private DiscountAudience audience;

    private Short percentOff;

    private Integer amountOffCents;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private DiscountDuration duration;

    // Solo con duration = repeating
    private Short durationInMonths;

    @Column(columnDefinition = "TEXT")
    private String providerCouponId;

    @NotNull
    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime validFrom = OffsetDateTime.now();

    private OffsetDateTime validUntil;

    private Integer maxRedemptions;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
