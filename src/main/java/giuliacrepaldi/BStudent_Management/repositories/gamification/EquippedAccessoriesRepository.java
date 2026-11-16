package giuliacrepaldi.BStudent_Management.repositories.gamification;

import java.util.List;
import giuliacrepaldi.BStudent_Management.entities.gamification.EquippedAccessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquippedAccessoriesRepository extends JpaRepository<EquippedAccessory, EquippedAccessory.EquippedAccessoryId> {

    List<EquippedAccessory> findById_StudentId(Long studentId);
}
