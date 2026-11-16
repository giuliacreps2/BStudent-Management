package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.EvolutionStageRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvolutionStageRulesRepository extends JpaRepository<EvolutionStageRule, Integer> {

    List<EvolutionStageRule> findAllByOrderByStageNumber();
}
