package giuliacrepaldi.BStudent_Management.repositories.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionStepsRepository extends JpaRepository<MissionStep, Long> {

    boolean existsByMission_MissionId(Long missionId);

    // In ordine, con lezione, esercizio e voce del banco già caricati
    @Query("""
            SELECT s FROM MissionStep s
            LEFT JOIN FETCH s.lesson
            LEFT JOIN FETCH s.missionExercise me LEFT JOIN FETCH me.questionBankEntry
            WHERE s.mission.missionId = :missionId
            ORDER BY s.stepSortOrder
            """)
    List<MissionStep> findByMissionOrdered(@Param("missionId") Long missionId);

    // 0 se la missione non ha passi: il nuovo va in coda con max + 1
    @Query("SELECT COALESCE(MAX(s.stepSortOrder), 0) FROM MissionStep s WHERE s.mission.missionId = :missionId")
    int findMaxSortOrder(@Param("missionId") Long missionId);

    // Il buco nell'ordine (es. 1, 3) non viola il vincolo: si sistema con il riordino
    @Modifying
    @Query("DELETE FROM MissionStep s WHERE s.lesson.lessonId = :lessonId")
    void deleteByLessonId(@Param("lessonId") Long lessonId);

    @Modifying
    @Query("DELETE FROM MissionStep s WHERE s.missionExercise.missionExerciseId = :missionExerciseId")
    void deleteByMissionExerciseId(@Param("missionExerciseId") Long missionExerciseId);
}
