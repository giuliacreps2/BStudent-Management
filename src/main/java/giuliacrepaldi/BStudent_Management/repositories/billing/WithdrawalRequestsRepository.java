package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawalRequestsRepository extends JpaRepository<WithdrawalRequest, Long> {

    // Un solo recesso per contratto
    boolean existsBySubscriptionSubscriptionId(Long subscriptionId);

    boolean existsByProductOrderProductOrderId(Long productOrderId);
}
