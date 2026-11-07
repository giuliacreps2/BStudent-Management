package giuliacrepaldi.BStudent_Management.repositories.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonsRepository extends JpaRepository<Lesson, Long> {

    boolean existsByMission_MissionId(Long missionId);

    long countByMission_MissionIdAndStatus(Long missionId, ContentStatus status);

    // Nell'ordine dei passi della missione
    @Query("""
            SELECT l FROM MissionStep s JOIN s.lesson l
            WHERE s.mission.missionId = :missionId
            ORDER BY s.stepSortOrder
            """)
    List<Lesson> findByMissionOrdered(@Param("missionId") Long missionId);
}
