package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.BillingEvent;
import giuliacrepaldi.BStudent_Management.enums.billing.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingEventsRepository extends JpaRepository<BillingEvent, Long> {

    // Idempotenza dei webhook: Stripe può reinviare lo stesso evento
    boolean existsByProviderAndProviderEventId(PaymentProvider provider, String providerEventId);
}
