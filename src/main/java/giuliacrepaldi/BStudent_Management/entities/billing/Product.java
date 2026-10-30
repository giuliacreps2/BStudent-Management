package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.enums.billing.ProductKind;
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

// Articolo acquistabile singolarmente (fase successiva all'MVP).
// Solo estetici: niente vantaggi nello studio in vendita, prezzo sempre in euro
@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(name = "uq_products_code", columnNames = "code"),
        @UniqueConstraint(name = "uq_products_provider_price", columnNames = "provider_price_id")
})
@Check(name = "chk_products_price", constraints = "price_cents > 0")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @NotBlank
    @Column(length = 60, nullable = false)
    private String code;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ProductKind kind;

    @Positive
    @Column(nullable = false)
    private int priceCents;

    @Builder.Default
    @Column(length = 3, nullable = false)
    private String currency = "EUR";

    @Column(columnDefinition = "TEXT")
    private String providerPriceId;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
