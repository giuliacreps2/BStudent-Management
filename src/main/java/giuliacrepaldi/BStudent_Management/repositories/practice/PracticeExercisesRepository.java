package giuliacrepaldi.BStudent_Management.repositories.practice;

import java.util.Optional;
import java.util.List;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeExercisesRepository extends JpaRepository<PracticeExercise, Long> {

    boolean existsByQuestionBankEntry_QuestionBankEntryId(Long questionBankEntryId);

    boolean existsByQuestionBankEntry_QuestionBankEntryIdAndStatus(Long questionBankEntryId, ContentStatus status);

    // Filtri dell'Admin, tutti facoltativi (null = nessun filtro). Pochi esercizi nell'MVP: niente Specification
    @Query("""
            SELECT p FROM PracticeExercise p
            WHERE (:subjectId IS NULL OR p.topic.subject.subjectId = :subjectId)
              AND (:topicId IS NULL OR p.topic.topicId = :topicId)
              AND (:skillId IS NULL OR p.skill.skillId = :skillId)
              AND (:status IS NULL OR p.status = :status)
            """)
    Page<PracticeExercise> search(@Param("subjectId") Long subjectId, @Param("topicId") Long topicId,
                                  @Param("skillId") Long skillId, @Param("status") ContentStatus status,
                                  Pageable pageable);

    // Voci che lo studente vede nella materia: pubblicate ed esercizio del banco non ritirato.
    // Per argomento, poi dalla più facile
    @Query("""
            SELECT p FROM PracticeExercise p
            JOIN FETCH p.questionBankEntry q JOIN FETCH p.topic t JOIN FETCH p.skill
            WHERE t.subject.subjectId = :subjectId
              AND p.status = giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus.published
              AND q.status <> giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus.withdrawn
            ORDER BY t.topicName, p.difficulty, p.practiceExerciseId
            """)
    List<PracticeExercise> findVisibleBySubject(@Param("subjectId") Long subjectId);

    @Query("""
            SELECT p FROM PracticeExercise p
            JOIN FETCH p.questionBankEntry q JOIN FETCH p.topic t JOIN FETCH p.skill
            WHERE t.topicId = :topicId
              AND p.status = giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus.published
              AND q.status <> giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus.withdrawn
            ORDER BY p.difficulty, p.practiceExerciseId
            """)
    List<PracticeExercise> findVisibleByTopic(@Param("topicId") Long topicId);

    // Stato personale per voce, dai tentativi della Palestra. Righe: voce, percentuale migliore, superata
    // (almeno la soglia), bozza salvata, in correzione, istante dell'ultima attività in secondi
    @Query(value = """
            SELECT pe.practice_exercise_id,
                   max(a.percentage),
                   coalesce(bool_or(a.percentage >= a.passing_threshold), false),
                   bool_or(ea.status = 'saved'),
                   bool_or(ea.status = 'pending_review'),
                   extract(epoch FROM max(coalesce(ea.completed_at, ea.submitted_at, ea.started_at)))
              FROM exercise_attempts ea
              JOIN practice_exercises pe ON pe.question_bank_entry_id = ea.question_bank_entry_id
              LEFT JOIN exercise_assessments a ON a.exercise_attempt_id = ea.exercise_attempt_id
             WHERE ea.student_id = :studentId AND ea.context = 'practice'
             GROUP BY pe.practice_exercise_id
            """, nativeQuery = true)
    List<Object[]> findPersonalStates(@Param("studentId") Long studentId);

    Optional<PracticeExercise> findByQuestionBankEntry_QuestionBankEntryId(Long questionBankEntryId);
}
