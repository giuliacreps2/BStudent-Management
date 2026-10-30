package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.enums.billing.PaymentProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Webhook ricevuto: ogni evento si elabora una sola volta (Stripe può reinviarlo).
// Nessun payload: contiene dati personali e Stripe permette di rileggere l'evento per id
@Entity
@Table(name = "billing_events", uniqueConstraints = {
        @UniqueConstraint(name = "uq_billing_events_provider", columnNames = {"provider", "provider_event_id"})
})
// Indice parziale idx_billing_events_unprocessed (processed_at IS NULL): solo lato DB
public class BillingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billingEventId;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private PaymentProvider provider = PaymentProvider.stripe;

    // evt_... di Stripe
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String providerEventId;

    // Es. invoice.paid, customer.subscription.updated
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventType;

    // Oggetto Stripe interessato (sub_..., in_..., ch_...)
    @Column(columnDefinition = "TEXT")
    private String objectId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime receivedAt;

    private OffsetDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String processingError;
}
