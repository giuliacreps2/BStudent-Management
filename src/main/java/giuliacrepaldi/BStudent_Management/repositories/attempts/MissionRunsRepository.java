package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRunsRepository extends JpaRepository<MissionRun, Long> {
}
