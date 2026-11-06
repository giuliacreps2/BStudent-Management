package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionExercisesRepository extends JpaRepository<MissionExercise, Long> {

    boolean existsByLesson_LessonId(Long lessonId);

    Optional<MissionExercise> findByLesson_LessonId(Long lessonId);

    // Lo stesso esercizio non si ripete nella stessa missione (blueprint §5.1)
    boolean existsByMission_MissionIdAndQuestionBankEntry_QuestionBankEntryId(Long missionId, Long questionBankEntryId);

    // Nell'ordine delle lezioni, che è l'ordine di svolgimento della missione
    @Query("""
            SELECT me FROM MissionExercise me JOIN FETCH me.lesson l
            WHERE me.mission.missionId = :missionId
            ORDER BY l.lessonSortOrder
            """)
    List<MissionExercise> findByMissionOrdered(@Param("missionId") Long missionId);

    boolean existsByQuestionBankEntry_QuestionBankEntryId(Long questionBankEntryId);

    // Esercizio collegato a una lezione nello stato indicato (es. pubblicata): non si ritira
    boolean existsByQuestionBankEntry_QuestionBankEntryIdAndLesson_Status(Long questionBankEntryId,
                                                                         ContentStatus lessonStatus);

    // Esercizi delle lezioni nello stato indicato, con lezione ed esercizio già caricati
    @Query("""
            SELECT me FROM MissionExercise me
            JOIN FETCH me.lesson l JOIN FETCH me.questionBankEntry
            WHERE me.mission.missionId = :missionId AND l.status = :lessonStatus
            ORDER BY l.lessonSortOrder
            """)
    List<MissionExercise> findByMissionAndLessonStatus(@Param("missionId") Long missionId,
                                                       @Param("lessonStatus") ContentStatus lessonStatus);
}
