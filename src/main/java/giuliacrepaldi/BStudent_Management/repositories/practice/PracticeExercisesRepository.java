package giuliacrepaldi.BStudent_Management.repositories.practice;

import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PracticeExercisesRepository extends JpaRepository<PracticeExercise, Long> {

    boolean existsByQuestionBankEntry_QuestionBankEntryId(Long questionBankEntryId);

    boolean existsByQuestionBankEntry_QuestionBankEntryIdAndStatus(Long questionBankEntryId, ContentStatus status);
}
