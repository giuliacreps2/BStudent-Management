package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionAnswersRepository extends JpaRepository<QuestionAnswer, Long> {

    List<QuestionAnswer> findByQuestion_QuestionIdOrderByAnswerSortOrder(Long questionId);

    // Eseguita subito sul DB: Hibernate farebbe gli INSERT delle nuove risposte prima dei DELETE
    // e (question_id, sort_order) verrebbe violato
    @Modifying
    @Query("DELETE FROM QuestionAnswer a WHERE a.question.questionId = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
}
