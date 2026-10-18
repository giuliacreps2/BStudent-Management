package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.entities.attempts.ContentReformulationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentReformulationReviewsRepository extends JpaRepository<ContentReformulationReview, Long> {
}
