package giuliacrepaldi.BStudent_Management.services.student.interfaces;

/**
 * Materie e corsi visti dallo studente (blueprint §5.8; frontend types/myCourses.ts, types/courseLanding.ts).
 *
 * <h2>TODO — metodi da implementare (Fase 3)</h2>
 * <pre>
 * CoursesPageDTO getCoursesPage(long studentId, String activeSubjectSlug);
 *     // materie pubblicate; senza corsi pubblicati -> "Presto disponibile" al posto della tagline.
 *     // Corsi visibili della materia attiva (default: la prima aperta) con livello, numero di tappe e di
 *     // skill calcolati, in evidenza (featured_until nel futuro), popolare = false fino al Blocco 5,
 *     // lucchetto premium. Ultimo risultato del test di livello, se c'è (IOrientationService)
 * CourseDetailDTO getCourseDetail(long studentId, String courseSlug);
 *     // pagina del corso: tagline, livello, citazione, missioni visibili; ricompense = lista vuota
 *     // (gamification) e livello/XP dello studente non restituiti (TODO gamification)
 * CourseLandingDTO getSubjectLanding(String subjectSlug);
 *     // pubblica, senza account: corso di riferimento (in evidenza più recente -> popolare -> primo
 *     // pubblicato) e le sue prime 5 missioni visibili; badge = numero di missioni ed esercizi.
 *     // Testi, FAQ, personaggi restano statici nel frontend; recensioni nascoste finché non sono vere
 * </pre>
 * Proposta §8.2 da applicare nel frontend: se non ci sono corsi del livello consigliato si mostrano tutti.
 * Controller: /student/courses/**; la landing pubblica in /public/subjects/{slug}/landing (permitAll).
 */
public interface IStudentCatalogService {
}
