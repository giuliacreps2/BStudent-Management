package giuliacrepaldi.BStudent_Management.repositories.notebook;

import giuliacrepaldi.BStudent_Management.entities.notebook.NotebookLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotebookLessonsRepository extends JpaRepository<NotebookLesson, NotebookLesson.NotebookLessonId> {
}
