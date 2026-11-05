package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionExercisesRepository extends JpaRepository<MissionExercise, Long> {

    boolean existsByLesson_LessonId(Long lessonId);

    // Esercizi delle lezioni nello stato indicato, con lezione ed esercizio già caricati
    @Query("""
            SELECT me FROM MissionExercise me
            JOIN FETCH me.lesson l JOIN FETCH me.questionBankEntry
            WHERE me.mission.missionId = :missionId AND l.status = :lessonStatus
            ORDER BY l.lessonSortOrder
            """)
    List<MissionExercise> findByMissionAndLessonStatus(@Param("missionId") Long missionId,
                                                       @Param("lessonStatus") ContentStatus lessonStatus);
}
