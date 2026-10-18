package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillLevelsRepository extends JpaRepository<SkillLevel, Long> {
}
