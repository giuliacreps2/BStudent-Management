package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.BillingCustomer;
import giuliacrepaldi.BStudent_Management.enums.billing.PaymentProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingCustomersRepository extends JpaRepository<BillingCustomer, Long> {

    Optional<BillingCustomer> findByUserUserId(Long userId);

    Optional<BillingCustomer> findByProviderAndProviderCustomerId(PaymentProvider provider, String providerCustomerId);
}
