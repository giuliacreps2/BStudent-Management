package giuliacrepaldi.BStudent_Management.services.student.interfaces;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionMapDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionStepDTO;

import java.util.List;

/**
 * Mappa del corso e pagina della missione viste dallo studente (blueprint §5.9; frontend types/missions.ts).
 * Lo svolgimento (avvio, tentativi, timer, checkpoint) è in IMissionRunsService; le tappe dello stepper
 * vengono dal percorso aperto (lezioni lette e checkpoint).
 *
 * <p>TODO: anteprima Admin delle bozze (?preview=true).
 */
public interface IStudentMissionsService {

    // Missioni visibili nell'ordine: superate = completate, la prima non superata = attuale, le altre bloccate
    MissionMapDTO getMissionMap(long studentId, String courseSlug);

    MissionPageDTO getMissionPage(long studentId, String courseSlug, String missionSlug);

    // Missione che lo studente può avviare: visibile, attuale o già completata, accessibile (premium).
    // Bloccata -> ConflictException; dell'abbonamento -> ForbiddenException; inesistente -> NotFoundException.
    // Una sola regola, usata dalla pagina e dallo svolgimento
    Mission findPlayableMission(long studentId, String courseSlug, String missionSlug);

    // Tappe con stato done | current | todo, dai passi visibili, dalle lezioni lette e dagli esercizi superati
    List<MissionStepDTO> stepper(List<MissionStep> visibleSteps, List<Long> readLessonIds, List<Long> passedExerciseIds);

    boolean isTutorialSeen(long studentId);

    // Tutorial della prima volta: mostrato una volta sola; ripetuta non cambia la data
    void markTutorialSeen(long studentId);
}
