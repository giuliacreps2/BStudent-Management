package giuliacrepaldi.BStudent_Management.repositories.notebook;

import giuliacrepaldi.BStudent_Management.entities.notebook.NotebookLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotebookLessonsRepository extends JpaRepository<NotebookLesson, NotebookLesson.NotebookLessonId> {

    // Lezioni del Quaderno con missione, corso, materia e argomento: le più recenti prima
    @Query("""
            SELECT nl FROM NotebookLesson nl
              JOIN FETCH nl.lesson l
              JOIN FETCH l.mission m
              JOIN FETCH m.course c
              JOIN FETCH c.subject
              LEFT JOIN FETCH l.topic
             WHERE nl.id.studentId = :studentId
             ORDER BY nl.acquiredAt DESC, l.lessonId DESC
            """)
    List<NotebookLesson> findByStudent(@Param("studentId") Long studentId);
}
