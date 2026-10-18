package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionCheckpointsRepository extends JpaRepository<MissionCheckpoint, MissionCheckpoint.MissionCheckpointId> {
}
