package giuliacrepaldi.BStudent_Management.services.catalog.interfaces;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.payloads.catalog.CourseDTO;
import org.springframework.data.domain.Page;

/**
 * Corsi della materia (blueprint §5.1).
 * Titolo, descrizione breve, presentazione estesa e copertina facoltativa.
 * findAll mostra tutti gli stati, perché è la vista dell'Admin.
 */
public interface ICoursesService {

    //Admin
    //CRUD -> create, read, update, delete

    Course saveNewCourse(long subjectId, CourseDTO body); // nasce draft

    Course findById(long courseId);

    Page<Course> findAll(int page, int size, String sortBy); // tutti gli stati: è la vista dell'Admin

    Page<Course> findBySubject(long subjectId, int page, int size, String sortBy);

    Course findByIdAndUpdate(long courseId, CourseDTO body);

    Course publishCourse(long courseId); // draft/withdrawn -> published

    Course withdrawCourse(long courseId); // published -> withdrawn

    void deleteCourse(long courseId); // solo draft mai pubblicato, altrimenti ConflictException
}
