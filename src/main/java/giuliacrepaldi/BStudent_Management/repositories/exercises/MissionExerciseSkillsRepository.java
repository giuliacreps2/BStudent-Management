package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface MissionExerciseSkillsRepository extends JpaRepository<MissionExerciseSkill, MissionExerciseSkill.MissionExerciseSkillId> {

    long countById_MissionExerciseId(Long missionExerciseId);

    // 0 se l'esercizio non ha pesi espliciti
    @Query("SELECT COALESCE(SUM(s.weight), 0) FROM MissionExerciseSkill s WHERE s.id.missionExerciseId = :id")
    BigDecimal sumWeights(@Param("id") Long missionExerciseId);
}
