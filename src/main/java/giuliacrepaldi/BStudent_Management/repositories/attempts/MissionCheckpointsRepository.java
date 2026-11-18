package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionCheckpointsRepository extends JpaRepository<MissionCheckpoint, MissionCheckpoint.MissionCheckpointId> {

    // Esercizi già superati nel percorso
    @Query("SELECT c.id.missionExerciseId FROM MissionCheckpoint c WHERE c.id.missionRunId = :runId")
    List<Long> findExerciseIds(@Param("runId") Long missionRunId);

    // Con il giudizio e il tentativo, per il risultato della missione
    @Query("""
            SELECT c FROM MissionCheckpoint c
            JOIN FETCH c.exerciseAssessment a JOIN FETCH a.exerciseAttempt
            WHERE c.id.missionRunId = :runId
            """)
    List<MissionCheckpoint> findWithAssessment(@Param("runId") Long missionRunId);
}
