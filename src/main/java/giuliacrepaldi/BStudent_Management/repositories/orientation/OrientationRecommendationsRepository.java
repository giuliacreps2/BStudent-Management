package giuliacrepaldi.BStudent_Management.repositories.orientation;

import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrientationRecommendationsRepository extends JpaRepository<OrientationRecommendation, OrientationRecommendation.OrientationRecommendationId> {

    boolean existsById_PracticeExerciseId(Long practiceExerciseId);

    // Con la voce e l'esercizio già caricati, per mostrarne il titolo
    @Query("""
            SELECT r FROM OrientationRecommendation r
            JOIN FETCH r.practiceExercise p JOIN FETCH p.questionBankEntry
            WHERE r.id.orientationOptionId IN :optionIds
            ORDER BY r.recommendationSortOrder
            """)
    List<OrientationRecommendation> findByOptionIds(@Param("optionIds") Collection<Long> optionIds);

    // Eseguita subito sul DB: Hibernate farebbe gli INSERT dei nuovi consigli prima dei DELETE
    @Modifying
    @Query("DELETE FROM OrientationRecommendation r WHERE r.id.orientationOptionId = :optionId")
    void deleteByOptionId(@Param("optionId") Long optionId);

    @Modifying
    @Query("DELETE FROM OrientationRecommendation r WHERE r.id.orientationOptionId IN :optionIds")
    void deleteByOptionIds(@Param("optionIds") Collection<Long> optionIds);
}
