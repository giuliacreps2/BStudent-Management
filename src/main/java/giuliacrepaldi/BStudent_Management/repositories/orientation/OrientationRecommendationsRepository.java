package giuliacrepaldi.BStudent_Management.repositories.orientation;

import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrientationRecommendationsRepository extends JpaRepository<OrientationRecommendation, OrientationRecommendation.OrientationRecommendationId> {
}
