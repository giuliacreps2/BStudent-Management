package giuliacrepaldi.BStudent_Management.services.catalog.interfaces;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.payloads.catalog.MissionDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.MissionReadinessDTO;

import java.util.List;

/**
 * Missioni del corso (blueprint §5.1).
 * Ordine positivo e unico nel corso; riordino atomico con gli ID del corso, senza duplicati.
 * <p>
 * - saveNewMission(courseId, …): la missione va in coda all'ordine.
 * - accessTier null = eredita dal corso; ammesse missioni premium anche in un corso free.
 * - reorderMissions(courseId, ids): riceve tutti e soli gli ID del corso, nel nuovo ordine, e li salva in modo atomico con SET CONSTRAINTS DEFERRED.
 * - publishMission: solo se la missione è pronta. withdrawMission, deleteMission
 * <p>
 * Sequenza libera (v0.8): la missione è una sequenza di passi, ognuno una lezione oppure un esercizio.
 * Lezioni ed esercizi entrano in coda alla sequenza quando vengono creati o collegati.
 * Lo studente vede le lezioni pubblicate e gli esercizi pubblicati nella missione e non ritirati nel banco.
 * Gli esercizi in bozza vengono pubblicati insieme alla missione; in una missione già pubblicata si pubblicano uno per uno.
 * Regole sulla sequenza che vede lo studente:
 * - inizia con una lezione; due lezioni di seguito sì, due esercizi di seguito no;
 * - per pubblicare serve almeno un esercizio; se l'ultimo passo non è un esercizio è solo un avviso;
 * - lo stesso esercizio non si ripete nella missione (vincolo UNIQUE).
 * In una missione pubblicata ogni modifica alla sequenza deve lasciarla valida, altrimenti ConflictException.
 */
public interface IMissionsService {

    //Admin
    //CRUD -> create, read, update, delete

    Mission saveNewMission(long courseId, MissionDTO body); // nasce draft, in coda all'ordine

    Mission findById(long missionId);

    List<Mission> findByCourse(long courseId); // ordinate per sortOrder, poche per corso: niente paginazione

    Mission findByIdAndUpdate(long missionId, MissionDTO body);

    // Tutti e soli gli ID delle missioni del corso, nel nuovo ordine: atomico con SET CONSTRAINTS DEFERRED
    List<Mission> reorderMissions(long courseId, List<Long> missionIds);

    //Sequenza

    List<MissionStep> findSteps(long missionId); // in ordine, tutti gli stati: è la vista dell'Admin

    // Tutti e soli gli ID dei passi della missione, nel nuovo ordine
    List<MissionStep> reorderSteps(long missionId, List<Long> stepIds);

    MissionReadinessDTO checkReadiness(long missionId); // problemi che bloccano la pubblicazione e avvisi

    // Nella transazione del chiamante, dopo una modifica alla sequenza: ConflictException se la missione
    // è pubblicata e la sequenza vista dallo studente non è più valida (la transazione va annullata)
    void assertSequenceStillValid(Mission mission);

    // Esercizio pubblicato con una versione pubblicata, skill a somma 100 o skill predefinita della materia
    List<String> exerciseProblems(MissionExercise exercise);

    //Stato

    Mission publishMission(long missionId); // draft/withdrawn -> published, solo se la missione è pronta

    Mission withdrawMission(long missionId); // published -> withdrawn

    void deleteMission(long missionId); // solo draft mai pubblicata e senza passi, altrimenti ConflictException
}
