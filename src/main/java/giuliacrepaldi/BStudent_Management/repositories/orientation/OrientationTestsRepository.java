package giuliacrepaldi.BStudent_Management.repositories.orientation;

import org.springframework.data.jpa.repository.Modifying;
import java.util.Optional;
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

    Optional<OrientationTest> findBySubject_SubjectIdAndKind(Long subjectId, OrientationTestKind kind);

    // Risultati salvati come fotografia (§8.3). Due INSERT distinti: il DB vuole esattamente un risultato
    // secondo il tipo del test
    @Modifying
    @Query(value = """
            INSERT INTO orientation_results (student_id, orientation_test_id, answers, result_level)
            VALUES (:studentId, :testId, CAST(:answers AS jsonb), :level)
            """, nativeQuery = true)
    void saveLevelResult(@Param("studentId") Long studentId, @Param("testId") Long testId,
                         @Param("answers") String answers, @Param("level") String level);

    // ids nel formato degli array di PostgreSQL: "{1,2,3}", "{}" se non c'è nessun consiglio
    @Modifying
    @Query(value = """
            INSERT INTO orientation_results (student_id, orientation_test_id, answers, recommended_practice_exercise_ids)
            VALUES (:studentId, :testId, CAST(:answers AS jsonb), CAST(:ids AS bigint[]))
            """, nativeQuery = true)
    void saveExercisesResult(@Param("studentId") Long studentId, @Param("testId") Long testId,
                             @Param("answers") String answers, @Param("ids") String ids);

    // Ultimo risultato dello studente. Riga: livello, voci consigliate separate da virgola, data ISO (UTC)
    @Query(value = """
            SELECT result_level, array_to_string(recommended_practice_exercise_ids, ','),
                   to_char(created_at AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"')
              FROM orientation_results
             WHERE student_id = :studentId AND orientation_test_id = :testId
             ORDER BY created_at DESC, orientation_result_id DESC
             LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findLastResult(@Param("studentId") Long studentId, @Param("testId") Long testId);
}
