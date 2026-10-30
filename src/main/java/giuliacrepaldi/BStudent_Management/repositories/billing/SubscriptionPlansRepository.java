package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlansRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByCode(String code);

    // Piano a partire dal Price ricevuto nei webhook di Stripe
    Optional<SubscriptionPlan> findByProviderPriceId(String providerPriceId);

    // Piani mostrati nella pagina dei prezzi
    List<SubscriptionPlan> findByActiveTrueOrderByMaxStudentsAscPriceCentsAsc();
}
