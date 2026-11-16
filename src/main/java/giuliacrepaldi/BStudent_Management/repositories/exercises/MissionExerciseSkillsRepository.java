package giuliacrepaldi.BStudent_Management.repositories.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MissionExerciseSkillsRepository extends JpaRepository<MissionExerciseSkill, MissionExerciseSkill.MissionExerciseSkillId> {

    long countById_MissionExerciseId(Long missionExerciseId);

    // 0 se l'esercizio non ha pesi espliciti
    @Query("SELECT COALESCE(SUM(s.weight), 0) FROM MissionExerciseSkill s WHERE s.id.missionExerciseId = :id")
    BigDecimal sumWeights(@Param("id") Long missionExerciseId);

    // Con la skill già caricata, per mostrarne codice e nome
    @Query("""
            SELECT s FROM MissionExerciseSkill s JOIN FETCH s.skill sk
            WHERE s.id.missionExerciseId = :id
            ORDER BY s.weight DESC, sk.skillId
            """)
    List<MissionExerciseSkill> findWithSkill(@Param("id") Long missionExerciseId);

    // Eseguita subito sul DB: Hibernate farebbe gli INSERT dei nuovi pesi prima dei DELETE
    // e la chiave (mission_exercise_id, skill_id) verrebbe violata
    @Modifying
    @Query("DELETE FROM MissionExerciseSkill s WHERE s.id.missionExerciseId = :id")
    void deleteByMissionExerciseId(@Param("id") Long missionExerciseId);

    // Punti skill massimi della missione per skill, prima del fattore delle missioni: punti dell'esercizio
    // per la quota della skill, sugli esercizi che lo studente vede. Righe: skill_id, nome, punti
    @Query(value = """
            SELECT s.skill_id, s.name, sum(me.max_points * mes.weight / 100)
              FROM mission_exercise_skills mes
              JOIN mission_exercises me ON me.mission_exercise_id = mes.mission_exercise_id
              JOIN question_bank_entries q ON q.question_bank_entry_id = me.question_bank_entry_id
              JOIN skills s ON s.skill_id = mes.skill_id
             WHERE me.mission_id = :missionId AND me.status = 'published' AND q.status <> 'withdrawn'
             GROUP BY s.skill_id, s.name
             ORDER BY 3 DESC, s.skill_id
            """, nativeQuery = true)
    List<Object[]> findMaxSkillPointsByMission(@Param("missionId") Long missionId);
}
