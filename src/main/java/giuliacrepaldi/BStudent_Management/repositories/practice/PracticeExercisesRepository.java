package giuliacrepaldi.BStudent_Management.repositories.practice;

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
}
