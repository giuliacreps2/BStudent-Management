package giuliacrepaldi.BStudent_Management.repositories.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
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

    // Missioni con un livello di accesso proprio (es. premium in un corso free)
    long countByCourse_CourseIdAndAccessTier(Long courseId, AccessTier accessTier);

    boolean existsByCourse_CourseIdAndSlug(Long courseId, String slug);

    boolean existsByCourse_CourseIdAndSlugAndMissionIdNot(Long courseId, String slug, Long missionId);

    // Prova finale già presente nel corso, esclusa la missione indicata (null alla creazione)
    @Query("SELECT COUNT(m) > 0 FROM Mission m WHERE m.course.courseId = :courseId AND m.isFinal = true"
            + " AND (:missionId IS NULL OR m.missionId <> :missionId)")
    boolean existsOtherFinal(@Param("courseId") Long courseId, @Param("missionId") Long missionId);

    // 0 se il corso non ha missioni: la nuova va in coda con max + 1
    @Query("SELECT COALESCE(MAX(m.missionSortOrder), 0) FROM Mission m WHERE m.course.courseId = :courseId")
    int findMaxSortOrder(@Param("courseId") Long courseId);
}
