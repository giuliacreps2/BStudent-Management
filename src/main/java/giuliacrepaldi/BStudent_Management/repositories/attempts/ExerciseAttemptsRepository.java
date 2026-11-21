package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import giuliacrepaldi.BStudent_Management.entities.attempts.ExerciseAttempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    // Blocca la riga fino alla fine della transazione: correzione della docente e "Annulla invio" non possono
    // accavallarsi. Va chiamato come prima lettura del tentativo nella transazione, altrimenti Hibernate
    // restituisce l'oggetto già caricato senza rileggerlo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ExerciseAttempt a WHERE a.exerciseAttemptId = :id")
    Optional<ExerciseAttempt> lockById(@Param("id") Long exerciseAttemptId);

    // Consegne in attesa della docente, dalla più vecchia. Materia facoltativa: per le missioni quella del
    // percorso, per la Palestra quella dell'argomento della voce
    @Query(value = """
            SELECT a FROM ExerciseAttempt a
            LEFT JOIN a.missionAttempt ma LEFT JOIN ma.missionRun r
            WHERE a.status = giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus.pending_review
              AND (:subjectId IS NULL
                   OR r.subject.subjectId = :subjectId
                   OR (a.context = giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext.practice
                       AND EXISTS (SELECT 1 FROM PracticeExercise pe
                                   WHERE pe.questionBankEntry = a.questionBankEntry
                                     AND pe.topic.subject.subjectId = :subjectId)))
            ORDER BY a.submittedAt, a.exerciseAttemptId
            """,
            countQuery = """
            SELECT COUNT(a) FROM ExerciseAttempt a
            LEFT JOIN a.missionAttempt ma LEFT JOIN ma.missionRun r
            WHERE a.status = giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus.pending_review
              AND (:subjectId IS NULL
                   OR r.subject.subjectId = :subjectId
                   OR (a.context = giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext.practice
                       AND EXISTS (SELECT 1 FROM PracticeExercise pe
                                   WHERE pe.questionBankEntry = a.questionBankEntry
                                     AND pe.topic.subject.subjectId = :subjectId)))
            """)
    Page<ExerciseAttempt> findPending(@Param("subjectId") Long subjectId, Pageable pageable);

    // Storico dei tentativi consegnati dello studente (blueprint §10.5), dal più recente. Righe: tentativo e
    // giudizio (null se in correzione o scaduto). context: 0 tutti, 1 missioni, 2 Palestra.
    // passed: 0 tutti, 1 superati, 2 non superati (in correzione e scaduti esclusi da 1 e 2)
    @Query(value = """
            SELECT a, s FROM ExerciseAttempt a
            LEFT JOIN ExerciseAssessment s ON s.exerciseAttemptId = a.exerciseAttemptId
            WHERE a.student.studentId = :studentId
              AND a.status IN :statuses
              AND COALESCE(a.submittedAt, a.completedAt, a.startedAt) >= :from
              AND (:context = 0
                   OR (:context = 1 AND a.context = giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext.mission)
                   OR (:context = 2 AND a.context = giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext.practice))
              AND (:passed = 0
                   OR (:passed = 1 AND s.percentage >= s.passingThreshold)
                   OR (:passed = 2 AND s.percentage < s.passingThreshold))
            ORDER BY COALESCE(a.submittedAt, a.completedAt, a.startedAt) DESC, a.exerciseAttemptId DESC
            """,
            countQuery = """
            SELECT COUNT(a) FROM ExerciseAttempt a
            LEFT JOIN ExerciseAssessment s ON s.exerciseAttemptId = a.exerciseAttemptId
            WHERE a.student.studentId = :studentId
              AND a.status IN :statuses
              AND COALESCE(a.submittedAt, a.completedAt, a.startedAt) >= :from
              AND (:context = 0
                   OR (:context = 1 AND a.context = giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext.mission)
                   OR (:context = 2 AND a.context = giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext.practice))
              AND (:passed = 0
                   OR (:passed = 1 AND s.percentage >= s.passingThreshold)
                   OR (:passed = 2 AND s.percentage < s.passingThreshold))
            """)
    Page<Object[]> findHistory(@Param("studentId") Long studentId,
                               @Param("statuses") Collection<ExerciseAttemptStatus> statuses,
                               @Param("from") OffsetDateTime from, @Param("context") int context,
                               @Param("passed") int passed, Pageable pageable);

    // Per voce della Palestra: tentativi valutati e l'ultimo valutato (per rivedere la correzione).
    // Righe: voce, numero di tentativi valutati, id dell'ultimo tentativo valutato
    @Query(value = """
            SELECT pe.practice_exercise_id, COUNT(s.exercise_attempt_id),
                   (array_agg(ea.exercise_attempt_id ORDER BY s.validated_at DESC)
                        FILTER (WHERE s.exercise_attempt_id IS NOT NULL))[1]
              FROM exercise_attempts ea
              JOIN practice_exercises pe ON pe.question_bank_entry_id = ea.question_bank_entry_id
              LEFT JOIN exercise_assessments s ON s.exercise_attempt_id = ea.exercise_attempt_id
             WHERE ea.student_id = :studentId AND ea.context = 'practice'
             GROUP BY pe.practice_exercise_id
            """, nativeQuery = true)
    List<Object[]> findPracticeAttemptStats(@Param("studentId") Long studentId);
}
