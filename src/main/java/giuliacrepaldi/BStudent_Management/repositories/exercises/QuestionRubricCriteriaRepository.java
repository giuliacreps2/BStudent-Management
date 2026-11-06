package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionRubricCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRubricCriteriaRepository extends JpaRepository<QuestionRubricCriterion, Long> {

    List<QuestionRubricCriterion> findByQuestion_QuestionIdOrderByCriterionSortOrder(Long questionId);

    // Eseguita subito sul DB, come per le risposte
    @Modifying
    @Query("DELETE FROM QuestionRubricCriterion c WHERE c.question.questionId = :questionId")
    void deleteByQuestionId(@Param("questionId") Long questionId);
}
