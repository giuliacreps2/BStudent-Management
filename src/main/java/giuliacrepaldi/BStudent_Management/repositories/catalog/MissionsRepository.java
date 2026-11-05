package giuliacrepaldi.BStudent_Management.repositories.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionsRepository extends JpaRepository<Mission, Long> {

    boolean existsByCourse_CourseId(Long courseId);

    boolean existsByCourse_CourseIdAndStatus(Long courseId, ContentStatus status);
}
