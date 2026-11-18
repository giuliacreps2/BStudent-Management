package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.MissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Repository
public interface MissionRecordsRepository extends JpaRepository<MissionRecord, MissionRecord.MissionRecordId> {

    // Migliori risultati dello studente nelle missioni indicate, per la media del corso
    @Query("""
            SELECT r.percentage FROM MissionRecord r
            WHERE r.id.studentId = :studentId AND r.id.missionId IN :missionIds
            """)
    List<BigDecimal> findPercentages(@Param("studentId") Long studentId, @Param("missionIds") Collection<Long> missionIds);
}
