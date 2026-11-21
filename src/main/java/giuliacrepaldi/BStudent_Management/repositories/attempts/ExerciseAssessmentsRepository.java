package giuliacrepaldi.BStudent_Management.repositories.attempts;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAssessment;
import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
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

    // Ultimo giudizio della docente in un tentativo di missione: lo studente lo vede riaprendo la missione
    // (Blueprint Svolgimento §4.3)
    Optional<ExerciseAssessment> findFirstByExerciseAttempt_MissionAttempt_MissionAttemptIdAndAssessmentMethodOrderByValidatedAtDesc(
            Long missionAttemptId, AssessmentMethod assessmentMethod);

    // Punti deboli (blueprint §10.5): argomenti della materia con esercizi valutati e mai superati, in
    // Palestra o in missione. Righe: argomento, nome, numero di esercizi da rivedere
    @Query(value = """
            SELECT t.topic_id, t.name, COUNT(*)
              FROM (SELECT ea.question_bank_entry_id AS entry_id,
                           bool_or(a.percentage >= a.passing_threshold) AS ever_passed
                      FROM exercise_assessments a
                      JOIN exercise_attempts ea ON ea.exercise_attempt_id = a.exercise_attempt_id
                     WHERE ea.student_id = :studentId
                     GROUP BY ea.question_bank_entry_id) x
              JOIN question_bank_entries q ON q.question_bank_entry_id = x.entry_id
              JOIN topics t ON t.topic_id = q.topic_id
             WHERE NOT x.ever_passed AND t.subject_id = :subjectId
             GROUP BY t.topic_id, t.name
             ORDER BY COUNT(*) DESC, t.name
            """, nativeQuery = true)
    List<Object[]> findWeakTopics(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);
}
