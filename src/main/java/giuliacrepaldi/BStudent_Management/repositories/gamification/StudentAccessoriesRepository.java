package giuliacrepaldi.BStudent_Management.repositories.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.StudentAccessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentAccessoriesRepository extends JpaRepository<StudentAccessory, StudentAccessory.StudentAccessoryId> {
}
