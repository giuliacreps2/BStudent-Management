package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.entities.progression.MissionSkillContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionSkillContributionsRepository extends JpaRepository<MissionSkillContribution, MissionSkillContribution.MissionSkillContributionId> {
}
