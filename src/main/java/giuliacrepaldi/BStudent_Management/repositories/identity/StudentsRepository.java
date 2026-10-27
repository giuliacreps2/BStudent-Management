package giuliacrepaldi.BStudent_Management.repositories.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentsRepository extends JpaRepository<Student, Long> {

    boolean existsByUsername(String username);

    Optional<Student> findByUserUserId(Long userId);
}
