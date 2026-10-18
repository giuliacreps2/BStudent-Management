package giuliacrepaldi.BStudent_Management.repositories.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.AccessoryChargeState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessoryChargeStatesRepository extends JpaRepository<AccessoryChargeState, AccessoryChargeState.AccessoryChargeStateId> {
}
