package giuliacrepaldi.BStudent_Management.enums.billing;

// Stati dell'abbonamento, allineati a quelli di Stripe: li aggiornano i webhook
public enum SubscriptionStatus {
    incomplete,
    incomplete_expired,
    trialing,
    active,
    past_due,
    unpaid,
    paused,
    canceled
}
