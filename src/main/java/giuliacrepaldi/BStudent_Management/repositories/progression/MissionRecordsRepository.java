package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.MissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionRecordsRepository extends JpaRepository<MissionRecord, MissionRecord.MissionRecordId> {
}
