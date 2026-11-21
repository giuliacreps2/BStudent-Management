package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.StudyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyEventsRepository extends JpaRepository<StudyEvent, Long> {

    // Lezioni del Quaderno riaperte dopo l'ingresso: le altre sono "Nuovo" (blueprint §14)
    @Query(value = """
            SELECT DISTINCT nl.lesson_id
              FROM notebook_lessons nl
              JOIN study_events e ON e.student_id = nl.student_id AND e.lesson_id = nl.lesson_id
             WHERE nl.student_id = :studentId AND e.kind = 'lesson_read' AND e.started_at >= nl.acquired_at
            """, nativeQuery = true)
    List<Long> findReopenedNotebookLessonIds(@Param("studentId") Long studentId);
}
