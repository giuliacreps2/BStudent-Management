package giuliacrepaldi.BStudent_Management.repositories.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.AccessoryBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessoryBenefitsRepository extends JpaRepository<AccessoryBenefit, Long> {
}
