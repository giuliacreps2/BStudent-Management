package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionsRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByProviderSubscriptionId(String providerSubscriptionId);

    // Area "I miei abbonamenti" di chi paga
    List<Subscription> findByPayerUserIdOrderByCreatedAtDesc(Long payerUserId);
}
