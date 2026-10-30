package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.DiscountRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRedemptionsRepository extends JpaRepository<DiscountRedemption, Long> {

    // Ogni sconto vale una volta per persona
    boolean existsByDiscountDiscountIdAndUserUserId(Long discountId, Long userId);

    long countByDiscountDiscountId(Long discountId);
}
