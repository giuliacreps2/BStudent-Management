package giuliacrepaldi.BStudent_Management.services.catalog.interfaces;

import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.payloads.catalog.MissionDTO;

import java.util.List;

/**
 * Missioni del corso (blueprint §5.1).
 * Ordine positivo e unico nel corso; riordino atomico con gli ID del corso, senza duplicati.
 * <p>
 * - saveNewMission(courseId, …): la missione va in coda all'ordine.
 * - accessTier null = eredita dal corso; ammesse missioni premium anche in un corso free.
 * - reorderMissions(courseId, ids): riceve tutti e soli gli ID del corso, nel nuovo ordine, e li salva in modo atomico con SET CONSTRAINTS DEFERRED.
 * - publishMission: solo se la missione è pronta. withdrawMission, deleteMission
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

    Mission publishMission(long missionId); // draft/withdrawn -> published, solo se la missione è pronta

    Mission withdrawMission(long missionId); // published -> withdrawn

    void deleteMission(long missionId); // solo draft mai pubblicata, altrimenti ConflictException
}
