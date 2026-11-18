package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRunLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRunLessonsRepository extends JpaRepository<MissionRunLesson, MissionRunLesson.MissionRunLessonId> {

    // Lezioni già lette nel percorso
    @Query("SELECT l.id.lessonId FROM MissionRunLesson l WHERE l.id.missionRunId = :runId")
    List<Long> findLessonIds(@Param("runId") Long missionRunId);
}
