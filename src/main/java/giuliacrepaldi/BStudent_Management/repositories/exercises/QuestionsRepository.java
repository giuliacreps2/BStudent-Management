package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionsRepository extends JpaRepository<Question, Long> {

    Optional<Question> findByQuestionVersion_QuestionVersionId(Long questionVersionId);

    // L'esercizio è corretto dalla docente? Conta la versione pubblicata; senza, l'ultima versione
    // (Blueprint Svolgimento §1: un esercizio così può stare solo in fondo alla missione)
    @Query("""
            SELECT COUNT(q) > 0 FROM Question q
            WHERE q.questionVersion.questionBankEntry.questionBankEntryId = :entryId
              AND q.assessmentMode = giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod.teacher
              AND (q.questionVersion.isPublished = true
                   OR (NOT EXISTS (SELECT 1 FROM QuestionVersion p
                                   WHERE p.questionBankEntry.questionBankEntryId = :entryId AND p.isPublished = true)
                       AND q.questionVersion.questionVersionNumber = (
                           SELECT MAX(v.questionVersionNumber) FROM QuestionVersion v
                           WHERE v.questionBankEntry.questionBankEntryId = :entryId)))
            """)
    boolean isTeacherAssessed(@Param("entryId") Long questionBankEntryId);
}
