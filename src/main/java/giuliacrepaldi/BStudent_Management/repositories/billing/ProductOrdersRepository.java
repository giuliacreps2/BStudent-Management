package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOrdersRepository extends JpaRepository<ProductOrder, Long> {

    Optional<ProductOrder> findByProviderCheckoutSessionId(String providerCheckoutSessionId);
}
