package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.PracticeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeRecordsRepository extends JpaRepository<PracticeRecord, PracticeRecord.PracticeRecordId> {
}
