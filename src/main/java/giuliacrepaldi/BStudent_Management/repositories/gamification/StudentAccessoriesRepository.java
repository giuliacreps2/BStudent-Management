package giuliacrepaldi.BStudent_Management.repositories.gamification;

import java.util.List;
import giuliacrepaldi.BStudent_Management.entities.gamification.StudentAccessory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentAccessoriesRepository extends JpaRepository<StudentAccessory, StudentAccessory.StudentAccessoryId> {

    List<StudentAccessory> findById_StudentId(Long studentId);

    boolean existsById_AccessoryId(Long accessoryId);
}
