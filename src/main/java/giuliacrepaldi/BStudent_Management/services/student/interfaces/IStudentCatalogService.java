package giuliacrepaldi.BStudent_Management.services.student.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.student.CourseDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.CourseLandingDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.CoursesPageDTO;

/**
 * Materie e corsi visti dallo studente (blueprint §5.8; frontend types/myCourses.ts, types/courseLanding.ts).
 *
 * <h2>TODO</h2>
 * <ul>
 *   <li>Proposta §8.2 da applicare nel frontend: se non ci sono corsi del livello consigliato si mostrano
 *       tutti.</li>
 *   <li>"Popolare": calcolato dall'uso reale, false fino al Blocco 5.</li>
 *   <li>Anteprima Admin delle bozze (?preview=true).</li>
 * </ul>
 */
public interface IStudentCatalogService {

    // Materie pubblicate (senza corsi pubblicati: "Presto disponibile") e corsi visibili della materia attiva.
    // activeSubjectSlug null o non valido: la prima materia aperta
    CoursesPageDTO getCoursesPage(long studentId, String activeSubjectSlug);

    CourseDetailDTO getCourseDetail(long studentId, String courseSlug);

    // Pubblica, senza account: corso di riferimento (in evidenza più recente, altrimenti il primo pubblicato)
    // e le sue prime 5 missioni visibili
    CourseLandingDTO getSubjectLanding(String subjectSlug);
}
