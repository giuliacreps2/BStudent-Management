package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionAttemptsRepository extends JpaRepository<MissionAttempt, Long> {
}
