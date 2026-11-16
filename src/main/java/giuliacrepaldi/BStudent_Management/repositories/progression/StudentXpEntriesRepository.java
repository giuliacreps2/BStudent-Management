package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.StudentXpEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface StudentXpEntriesRepository extends JpaRepository<StudentXpEntry, Long> {

    boolean existsByStudentIdAndMissionId(Long studentId, Long missionId);

    // XP dello studente = somma del registro; 0 se non ha ancora superato missioni
    @Query("SELECT COALESCE(SUM(e.xp), 0) FROM StudentXpEntry e WHERE e.studentId = :studentId")
    BigDecimal sumXp(@Param("studentId") Long studentId);
}
