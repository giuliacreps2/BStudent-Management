package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionVersionsRepository extends JpaRepository<QuestionVersion, Long> {

    @Query("""
            SELECT COUNT(v) > 0 FROM QuestionVersion v
            WHERE v.questionBankEntry.questionBankEntryId = :entryId AND v.isPublished = true
            """)
    boolean existsPublishedVersion(@Param("entryId") Long questionBankEntryId);
}
