package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundsRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByProviderRefundId(String providerRefundId);
}
