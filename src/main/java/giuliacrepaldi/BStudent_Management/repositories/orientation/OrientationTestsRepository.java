package giuliacrepaldi.BStudent_Management.repositories.orientation;

import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationTest;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrientationTestsRepository extends JpaRepository<OrientationTest, Long> {

    List<OrientationTest> findBySubject_SubjectIdOrderByKind(Long subjectId);

    boolean existsBySubject_SubjectIdAndKind(Long subjectId, OrientationTestKind kind);

    // orientation_results non ha ancora un'entity (Fase 3): letture native
    @Query(value = "SELECT EXISTS (SELECT 1 FROM orientation_results WHERE orientation_test_id = :testId)",
            nativeQuery = true)
    boolean hasResults(@Param("testId") Long testId);

    // answers = {"<questionId>": <optionId>}: un'opzione scelta in un risultato salvato non si cancella (§8.4)
    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM orientation_results r, jsonb_each_text(r.answers) a
                           WHERE a.value::bigint IN (:optionIds))
            """, nativeQuery = true)
    boolean isAnyOptionChosen(@Param("optionIds") Collection<Long> optionIds);
}
