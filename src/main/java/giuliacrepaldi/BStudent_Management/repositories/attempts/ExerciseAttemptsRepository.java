package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.Collection;
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
}
