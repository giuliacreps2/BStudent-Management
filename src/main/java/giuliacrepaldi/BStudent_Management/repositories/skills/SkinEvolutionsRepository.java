package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.SkinEvolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkinEvolutionsRepository extends JpaRepository<SkinEvolution, Long> {

    List<SkinEvolution> findBySkin_SkinIdOrderByStageNumber(Long skinId);

    Optional<SkinEvolution> findBySkin_SkinIdAndStageNumber(Long skinId, int stageNumber);
}
