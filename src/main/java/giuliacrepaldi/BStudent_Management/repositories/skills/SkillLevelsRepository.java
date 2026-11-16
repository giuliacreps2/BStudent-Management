package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SkillLevelsRepository extends JpaRepository<SkillLevel, Long> {

    List<SkillLevel> findBySkill_SkillIdOrderByLevelNumber(Long skillId);

    // Le soglie di più skill in una sola lettura, in ordine di livello
    @Query("SELECT l FROM SkillLevel l WHERE l.skill.skillId IN :skillIds ORDER BY l.skill.skillId, l.levelNumber")
    List<SkillLevel> findBySkillIds(@Param("skillIds") Collection<Long> skillIds);

    @Modifying
    @Query("DELETE FROM SkillLevel l WHERE l.skill.skillId = :skillId")
    void deleteBySkillId(@Param("skillId") Long skillId);
}
