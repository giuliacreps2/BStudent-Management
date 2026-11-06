package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseAttemptsRepository extends JpaRepository<ExerciseAttempt, Long> {

    // Una versione già svolta non si modifica né si cancella: lo storico la usa come riferimento
    boolean existsByQuestionVersion_QuestionVersionId(Long questionVersionId);

    // Un esercizio di missione già svolto non si scollega: tentativi e checkpoint lo usano come riferimento
    boolean existsByMissionExercise_MissionExerciseId(Long missionExerciseId);
}
