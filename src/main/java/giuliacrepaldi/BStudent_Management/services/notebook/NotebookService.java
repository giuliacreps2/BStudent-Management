package giuliacrepaldi.BStudent_Management.services.notebook;

import giuliacrepaldi.BStudent_Management.entities.notebook.NotebookLesson;
import giuliacrepaldi.BStudent_Management.repositories.catalog.LessonsRepository;
import giuliacrepaldi.BStudent_Management.repositories.identity.StudentsRepository;
import giuliacrepaldi.BStudent_Management.repositories.notebook.NotebookLessonsRepository;
import giuliacrepaldi.BStudent_Management.services.notebook.interfaces.INotebookService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotebookService implements INotebookService {

    private final NotebookLessonsRepository notebookLessonsRepository;
    private final LessonsRepository lessonsRepository;
    private final StudentsRepository studentsRepository;
    private final EntityManager entityManager;

    public NotebookService(NotebookLessonsRepository notebookLessonsRepository, LessonsRepository lessonsRepository,
                           StudentsRepository studentsRepository, EntityManager entityManager) {
        this.notebookLessonsRepository = notebookLessonsRepository;
        this.lessonsRepository = lessonsRepository;
        this.studentsRepository = studentsRepository;
        this.entityManager = entityManager;
    }

    // La visibilità della lezione la controlla chi chiama (lo svolgimento: è il passo attuale della missione)
    @Override
    @Transactional
    public void acquireLesson(long studentId, long lessonId) {
        NotebookLesson.NotebookLessonId id = new NotebookLesson.NotebookLessonId(studentId, lessonId);
        if (notebookLessonsRepository.existsById(id)) {
            return;
        }
        // persist e non save: con la chiave composta già valorizzata save farebbe un merge
        entityManager.persist(NotebookLesson.builder().id(id)
                .student(studentsRepository.getReferenceById(studentId))
                .lesson(lessonsRepository.getReferenceById(lessonId))
                .build());
    }
}
