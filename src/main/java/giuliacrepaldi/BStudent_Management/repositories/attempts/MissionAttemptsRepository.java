package giuliacrepaldi.BStudent_Management.repositories.attempts;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;
import java.util.Collection;
import java.util.Optional;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionAttemptsRepository extends JpaRepository<MissionAttempt, Long> {

    // Tentativo di missione in corso (attivo, salvato o in correzione)
    boolean existsByStudent_StudentIdAndStatusIn(Long studentId, Collection<MissionAttemptStatus> statuses);

    // Ultimo tentativo del percorso: quello aperto, oppure l'ultimo fallito da ritentare
    Optional<MissionAttempt> findFirstByMissionRun_MissionRunIdOrderByAttemptNumberDesc(Long missionRunId);

    // Missioni del corso che lo studente ha superato almeno una volta
    @Query(value = """
            SELECT DISTINCT mr.mission_id
              FROM mission_attempts ma
              JOIN mission_runs mr ON mr.mission_run_id = ma.mission_run_id
              JOIN missions m ON m.mission_id = mr.mission_id
             WHERE ma.student_id = :studentId AND ma.status = 'passed' AND m.course_id = :courseId
            """, nativeQuery = true)
    List<Long> findPassedMissionIds(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
