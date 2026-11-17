package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

/**
 * Risultato del corso e prima evoluzione (blueprint principale §10.4; Blueprint Gamification §4, §8-bis).
 * Media dei migliori risultati delle missioni, con lo stesso peso per ogni missione; un risultato peggiore
 * non la abbassa. Corso completato quando tutte le missioni visibili sono superate: "completato" vuol dire
 * tutte, senza completamento ridotto per chi non paga. Ogni materia ha un corso d'ingresso tutto gratuito
 * (courses.is_entry_course), che dà la prima evoluzione a chiunque.
 *
 * <h2>TODO — metodi da implementare (Blocco 5)</h2>
 * <pre>
 * CourseResultDTO onMissionPassed(long studentId, long missionId);
 *     // ricalcola course_results (missioni superate su visibili, media); alla prima volta che il corso
 *     // risulta completato chiama IStudentXpService.onCourseCompleted (Evoluzione I e 7 vite, una volta sola)
 * </pre>
 * TODO (Admin dei corsi): una missione nuova pubblicata in un corso già completato non toglie il
 * completamento a chi lo aveva; la percentuale di avanzamento torna sotto il 100%.
 */
public interface ICourseProgressService {
}
