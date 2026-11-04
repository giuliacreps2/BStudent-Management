package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionExerciseSkillsRepository extends JpaRepository<MissionExerciseSkill, MissionExerciseSkill.MissionExerciseSkillId> {
}
