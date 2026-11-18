package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.progression.CourseResultDTO;

/**
 * Risultato del corso e prima evoluzione (blueprint principale §10.4; Blueprint Gamification §4, §8-bis).
 * Media dei migliori risultati delle missioni, con lo stesso peso per ogni missione; un risultato peggiore
 * non la abbassa. Corso completato quando tutte le missioni visibili sono superate: "completato" vuol dire
 * tutte, senza completamento ridotto per chi non paga. Ogni materia ha un corso d'ingresso tutto gratuito
 * (courses.is_entry_course), che dà la prima evoluzione a chiunque.
 *
 * <p>TODO (Admin dei corsi): una missione nuova pubblicata in un corso già completato. Il DB vuole che un corso
 * completato abbia tutte le missioni superate (chk_course_results_completed), quindi oggi il ricalcolo toglie la
 * data di completamento finché lo studente non supera anche la nuova. L'Evoluzione I non si perde (gli stadi
 * restano) e non si riassegna. Da decidere se mostrare "completato" anche in quel caso.
 */
public interface ICourseProgressService {

    // Dopo ogni missione superata (MissionProgressService): ricalcola course_results; la prima volta che il
    // corso risulta completato chiama IStudentXpService.onCourseCompleted (Evoluzione I e 7 vite, una volta sola)
    CourseResultDTO onMissionPassed(long studentId, long missionId);
}
