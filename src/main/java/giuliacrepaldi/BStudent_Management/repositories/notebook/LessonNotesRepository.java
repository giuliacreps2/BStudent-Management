package giuliacrepaldi.BStudent_Management.repositories.notebook;

import giuliacrepaldi.BStudent_Management.entities.notebook.LessonNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonNotesRepository extends JpaRepository<LessonNote, LessonNote.LessonNoteId> {

    List<LessonNote> findById_StudentId(Long studentId);
}
