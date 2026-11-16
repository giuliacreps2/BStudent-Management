package giuliacrepaldi.BStudent_Management.repositories.attempts;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseAssessmentsRepository extends JpaRepository<ExerciseAssessment, Long> {

    // Tentativi valutati della Palestra, in ordine di valutazione. Superato = percentuale almeno pari alla
    // soglia salvata con il giudizio (60%). Righe: tentativo, voce della Palestra, istante della valutazione
    // in secondi, superato. Per la beta si legge tutto lo storico dello studente
    @Query(value = """
            SELECT a.exercise_attempt_id, pe.practice_exercise_id,
                   extract(epoch FROM a.validated_at), a.percentage >= a.passing_threshold
              FROM exercise_assessments a
              JOIN exercise_attempts ea ON ea.exercise_attempt_id = a.exercise_attempt_id
              JOIN practice_exercises pe ON pe.question_bank_entry_id = ea.question_bank_entry_id
             WHERE ea.student_id = :studentId AND ea.context = 'practice'
             ORDER BY a.validated_at, a.exercise_attempt_id
            """, nativeQuery = true)
    List<Object[]> findPracticeOutcomes(@Param("studentId") Long studentId);

    // Giorni (ora italiana, formato AAAA-MM-GG) con almeno un esercizio superato, in Palestra o in missione
    @Query(value = """
            SELECT DISTINCT to_char((a.validated_at AT TIME ZONE 'Europe/Rome')::date, 'YYYY-MM-DD')
              FROM exercise_assessments a
              JOIN exercise_attempts ea ON ea.exercise_attempt_id = a.exercise_attempt_id
             WHERE ea.student_id = :studentId AND a.percentage >= a.passing_threshold
            """, nativeQuery = true)
    List<String> findStudyDays(@Param("studentId") Long studentId);
}
