package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionExercisesRepository extends JpaRepository<MissionExercise, Long> {

    // Lo stesso esercizio non si ripete nella stessa missione (blueprint §5.1)
    boolean existsByMission_MissionIdAndQuestionBankEntry_QuestionBankEntryId(Long missionId, Long questionBankEntryId);

    // Nell'ordine dei passi, che è l'ordine di svolgimento della missione
    @Query("""
            SELECT me FROM MissionStep s JOIN s.missionExercise me JOIN FETCH me.questionBankEntry
            WHERE s.mission.missionId = :missionId
            ORDER BY s.stepSortOrder
            """)
    List<MissionExercise> findByMissionOrdered(@Param("missionId") Long missionId);

    boolean existsByQuestionBankEntry_QuestionBankEntryId(Long questionBankEntryId);

    // Missioni in cui l'esercizio è usato, con missione e corso già caricati
    @Query("""
            SELECT me FROM MissionExercise me JOIN FETCH me.mission m JOIN FETCH m.course
            WHERE me.questionBankEntry.questionBankEntryId = :entryId
            ORDER BY m.course.courseId, m.missionSortOrder
            """)
    List<MissionExercise> findByEntryWithMission(@Param("entryId") Long questionBankEntryId);

    // Altri esercizi che lo studente vede o vedrà (non ritirati; in una missione pubblicata solo quelli
    // pubblicati): 0 = senza questo la missione resta senza esercizi
    @Query("""
            SELECT COUNT(me) FROM MissionExercise me
            WHERE me.mission.missionId = :missionId
              AND me.questionBankEntry.questionBankEntryId <> :entryId
              AND me.questionBankEntry.status <> giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus.withdrawn
              AND (me.status = giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus.published
                   OR me.mission.status <> giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus.published)
            """)
    long countOtherVisible(@Param("missionId") Long missionId, @Param("entryId") Long questionBankEntryId);
}
