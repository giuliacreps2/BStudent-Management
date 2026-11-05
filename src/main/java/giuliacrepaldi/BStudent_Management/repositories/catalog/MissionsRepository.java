package giuliacrepaldi.BStudent_Management.repositories.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionsRepository extends JpaRepository<Mission, Long> {

    boolean existsByCourse_CourseId(Long courseId);

    boolean existsByCourse_CourseIdAndStatus(Long courseId, ContentStatus status);

    long countByCourse_CourseIdAndStatus(Long courseId, ContentStatus status);

    List<Mission> findByCourse_CourseIdOrderByMissionSortOrder(Long courseId);

    // 0 se il corso non ha missioni: la nuova va in coda con max + 1
    @Query("SELECT COALESCE(MAX(m.missionSortOrder), 0) FROM Mission m WHERE m.course.courseId = :courseId")
    int findMaxSortOrder(@Param("courseId") Long courseId);
}
