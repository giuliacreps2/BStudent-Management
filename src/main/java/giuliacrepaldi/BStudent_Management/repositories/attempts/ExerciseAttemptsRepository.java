package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.Collection;
import java.util.Optional;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseAttemptsRepository extends JpaRepository<ExerciseAttempt, Long> {

    // Una versione già svolta non si modifica né si cancella: lo storico la usa come riferimento
    boolean existsByQuestionVersion_QuestionVersionId(Long questionVersionId);

    // Un esercizio di missione già svolto non si scollega: tentativi e checkpoint lo usano come riferimento
    boolean existsByMissionExercise_MissionExerciseId(Long missionExerciseId);

    // Tentativo aperto della Palestra per lo stesso esercizio: al massimo uno (uq_open_practice_attempt)
    @Query("""
            SELECT a FROM ExerciseAttempt a
            WHERE a.student.studentId = :studentId
              AND a.questionBankEntry.questionBankEntryId = :entryId
              AND a.context = giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext.practice
              AND a.status IN :statuses
            """)
    Optional<ExerciseAttempt> findOpenPractice(@Param("studentId") Long studentId, @Param("entryId") Long entryId,
                                               @Param("statuses") Collection<ExerciseAttemptStatus> statuses);

    // Esercizio aperto del tentativo di missione: al massimo uno (uq_open_mission_exercise_attempt)
    Optional<ExerciseAttempt> findFirstByMissionAttempt_MissionAttemptIdAndStatusIn(Long missionAttemptId,
                                                                                   Collection<ExerciseAttemptStatus> statuses);
}
