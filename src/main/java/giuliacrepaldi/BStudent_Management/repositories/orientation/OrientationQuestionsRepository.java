package giuliacrepaldi.BStudent_Management.repositories.orientation;

import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrientationQuestionsRepository extends JpaRepository<OrientationQuestion, Long> {

    List<OrientationQuestion> findByOrientationTest_OrientationTestIdOrderByQuestionSortOrder(Long testId);

    // 0 se il test non ha domande: la nuova va in coda con max + 1
    @Query("SELECT COALESCE(MAX(q.questionSortOrder), 0) FROM OrientationQuestion q WHERE q.orientationTest.orientationTestId = :testId")
    int findMaxSortOrder(@Param("testId") Long testId);
}
