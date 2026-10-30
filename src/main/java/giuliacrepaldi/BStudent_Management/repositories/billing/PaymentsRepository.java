package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByProviderInvoiceId(String providerInvoiceId);

    Optional<Payment> findByProviderPaymentIntentId(String providerPaymentIntentId);

    // Storico di primo addebito e rinnovi
    List<Payment> findBySubscriptionSubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);
}
