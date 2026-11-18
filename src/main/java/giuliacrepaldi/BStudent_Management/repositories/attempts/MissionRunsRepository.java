package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionRun;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRunsRepository extends JpaRepository<MissionRun, Long> {

    // Percorso aperto della missione (in corso o in attesa della docente): al massimo uno per missione,
    // perché start() riprende quello esistente invece di crearne un altro
    @Query("""
            SELECT r FROM MissionRun r
            WHERE r.student.studentId = :studentId AND r.mission.missionId = :missionId AND r.status IN :statuses
            ORDER BY r.missionRunId DESC
            """)
    List<MissionRun> findByMission(@Param("studentId") Long studentId, @Param("missionId") Long missionId,
                                   @Param("statuses") Collection<MissionRunStatus> statuses);

    // Missione in corso nella materia: al massimo una (uq_open_mission_run, v0.11)
    Optional<MissionRun> findFirstByStudent_StudentIdAndSubject_SubjectIdAndStatus(Long studentId, Long subjectId,
                                                                                  MissionRunStatus status);

    // "Continua la missione": percorsi aperti dello studente, in tutte le materie
    List<MissionRun> findByStudent_StudentIdAndStatusInOrderByMissionRunId(Long studentId,
                                                                          Collection<MissionRunStatus> statuses);
}
