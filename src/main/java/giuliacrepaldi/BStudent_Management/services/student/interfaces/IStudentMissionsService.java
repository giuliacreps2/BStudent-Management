package giuliacrepaldi.BStudent_Management.services.student.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.student.MissionMapDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionPageDTO;

/**
 * Mappa del corso e pagina della missione viste dallo studente (blueprint §5.9; frontend types/missions.ts).
 * Lo svolgimento (avvio, tentativi, timer, checkpoint) è del Blocco 5, non di qui.
 *
 * <p>TODO (Blocco 5): lo stato delle tappe nello stepper arriva dai checkpoint; oggi la prima è l'attuale.
 * TODO: anteprima Admin delle bozze (?preview=true).
 */
public interface IStudentMissionsService {

    // Missioni visibili nell'ordine: superate = completate, la prima non superata = attuale, le altre bloccate
    MissionMapDTO getMissionMap(long studentId, String courseSlug);

    MissionPageDTO getMissionPage(long studentId, String courseSlug, String missionSlug);

    boolean isTutorialSeen(long studentId);

    // Tutorial della prima volta: mostrato una volta sola; ripetuta non cambia la data
    void markTutorialSeen(long studentId);
}
