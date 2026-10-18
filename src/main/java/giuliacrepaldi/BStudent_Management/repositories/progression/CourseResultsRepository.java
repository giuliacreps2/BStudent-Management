package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.CourseResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseResultsRepository extends JpaRepository<CourseResult, CourseResult.CourseResultId> {
}
