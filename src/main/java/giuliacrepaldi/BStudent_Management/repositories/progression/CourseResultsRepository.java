package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.CourseResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseResultsRepository extends JpaRepository<CourseResult, CourseResult.CourseResultId> {

    // Risultati dei corsi dello studente con il corso (per "I miei progressi")
    @Query("SELECT r FROM CourseResult r JOIN FETCH r.course WHERE r.id.studentId = :studentId")
    List<CourseResult> findByStudent(@Param("studentId") Long studentId);
}
