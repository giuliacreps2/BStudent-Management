package giuliacrepaldi.BStudent_Management.repositories.orientation;

import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrientationOptionsRepository extends JpaRepository<OrientationOption, Long> {

    List<OrientationOption> findByOrientationQuestion_OrientationQuestionIdOrderByOptionSortOrder(Long questionId);

    @Query("""
            SELECT o FROM OrientationOption o
            WHERE o.orientationQuestion.orientationQuestionId IN :questionIds
            ORDER BY o.orientationQuestion.orientationQuestionId, o.optionSortOrder
            """)
    List<OrientationOption> findByQuestionIds(@Param("questionIds") Collection<Long> questionIds);

    // 0 se la domanda non ha opzioni: la nuova va in coda con max + 1
    @Query("SELECT COALESCE(MAX(o.optionSortOrder), 0) FROM OrientationOption o WHERE o.orientationQuestion.orientationQuestionId = :questionId")
    int findMaxSortOrder(@Param("questionId") Long questionId);
}
