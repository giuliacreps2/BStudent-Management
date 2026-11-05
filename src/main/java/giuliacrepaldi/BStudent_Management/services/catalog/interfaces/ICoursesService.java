package giuliacrepaldi.BStudent_Management.services.catalog.interfaces;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.payloads.catalog.CourseDTO;
import org.springframework.data.domain.Page;

/**
 * Corsi della materia (blueprint §5.1).
 * Titolo, descrizione breve, presentazione estesa e copertina facoltativa.
 * findAll mostra tutti gli stati, perché è la vista dell'Admin.
 * <p>
 * TODO "In arrivo": l'Admin annuncia un corso ancora in bozza e lo studente lo vede nel catalogo
 * con solo copertina, titolo e descrizione breve, col lucchetto e senza missioni né lezioni.
 * Proposta: colonna courses.announced_at (migrazione) e metodi announceCourse/unannounceCourse;
 * draft + announced_at = teaser. Niente date di uscita promesse se non certe (pubblicità ingannevole).
 * <p>
 * TODO anteprima (Blocco 4): ?preview=true sugli endpoint studente accettato solo per l'ADMIN;
 * mostra anche bozze e premium con un banner "non visibile agli studenti" e non salva tentativi
 * né progressi. Per gli altri ruoli il parametro è ignorato.
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
