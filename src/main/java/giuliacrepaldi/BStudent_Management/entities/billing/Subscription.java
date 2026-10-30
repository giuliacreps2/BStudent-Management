package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.billing.SubscriptionEndReason;
import giuliacrepaldi.BStudent_Management.enums.billing.SubscriptionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Contratto di abbonamento: lo conclude e lo paga un adulto (payer), ne beneficiano
// uno o due studenti (SubscriptionStudent). Lo stato lo aggiornano i webhook di Stripe
@Entity
@Table(name = "subscriptions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_subscriptions_provider", columnNames = "provider_subscription_id")
}, indexes = {
        @Index(name = "idx_subscriptions_payer", columnList = "payer_user_id")
})
// Indice parziale idx_subscriptions_period_end (ended_at IS NULL): solo lato DB
@Check(name = "chk_subscriptions_period", constraints = """
        current_period_end IS NULL OR current_period_start IS NULL OR current_period_end > current_period_start""")
@Check(name = "chk_subscriptions_ended", constraints = "ended_at IS NULL OR end_reason IS NOT NULL")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_user_id", nullable = false)
    private User payer;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private SubscriptionStatus status = SubscriptionStatus.incomplete;

    // sub_... di Stripe: null finché il checkout non è completato
    @Column(columnDefinition = "TEXT")
    private String providerSubscriptionId;

    private OffsetDateTime startedAt;

    private OffsetDateTime currentPeriodStart;

    private OffsetDateTime currentPeriodEnd;

    // Disdetta: niente rinnovo, accesso fino alla fine del periodo già pagato
    @Column(nullable = false)
    private boolean cancelAtPeriodEnd;

    private OffsetDateTime canceledAt;

    private OffsetDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private SubscriptionEndReason endReason;

    // Recesso entro 14 giorni dalla conclusione (art. 52 Codice del consumo)
    private OffsetDateTime withdrawalDeadline;

    // Esecuzione immediata richiesta: in caso di recesso si paga la parte già fruita (art. 57 c. 3)
    private OffsetDateTime immediateStartConsentAt;

    // Condizioni di vendita accettate (registrate anche in policy_acceptances)
    @NotBlank
    @Column(length = 40, nullable = false)
    private String termsVersion;

    // Promemoria prima del rinnovo dei piani annuali
    private OffsetDateTime renewalReminderSentAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
