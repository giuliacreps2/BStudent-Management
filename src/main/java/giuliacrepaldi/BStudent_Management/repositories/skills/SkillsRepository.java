package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SkillsRepository extends JpaRepository<Skill, Long> {

    boolean existsByCodeSkill(String codeSkill);

    // Tutte le tabelle con una FK verso skills: se una la usa, la skill non si cancella.
    // Da aggiornare se una nuova tabella punta a skills
    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM mission_exercise_skills WHERE skill_id = :id)
                OR EXISTS (SELECT 1 FROM subjects WHERE default_skill_id = :id)
                OR EXISTS (SELECT 1 FROM practice_exercises WHERE skill_id = :id)
                OR EXISTS (SELECT 1 FROM accessories WHERE skill_id = :id)
                OR EXISTS (SELECT 1 FROM skill_levels WHERE skill_id = :id)
                OR EXISTS (SELECT 1 FROM mission_skill_contributions WHERE skill_id = :id)
                OR EXISTS (SELECT 1 FROM practice_records WHERE skill_id = :id)
                OR EXISTS (SELECT 1 FROM skill_credit_entries WHERE skill_id = :id)
            """, nativeQuery = true)
    boolean isInUse(@Param("id") Long skillId);
}
