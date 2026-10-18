package giuliacrepaldi.BStudent_Management.repositories.practice;

import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicsRepository extends JpaRepository<Topic, Long> {
}
