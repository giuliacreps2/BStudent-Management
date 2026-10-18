package giuliacrepaldi.BStudent_Management.repositories.orientation;

import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrientationQuestionsRepository extends JpaRepository<OrientationQuestion, Long> {
}
