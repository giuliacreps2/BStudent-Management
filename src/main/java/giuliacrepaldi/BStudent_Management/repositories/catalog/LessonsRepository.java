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

    boolean existsByMission_MissionIdAndStatus(Long missionId, ContentStatus status);

    // Lezioni nello stato indicato senza esercizio collegato: bloccano la pubblicazione della missione
    @Query("""
            SELECT l.lessonTitle FROM Lesson l
            WHERE l.mission.missionId = :missionId AND l.status = :status
              AND NOT EXISTS (SELECT me FROM MissionExercise me WHERE me.lesson = l)
            ORDER BY l.lessonSortOrder
            """)
    List<String> findTitlesWithoutExercise(@Param("missionId") Long missionId,
                                           @Param("status") ContentStatus status);
}
