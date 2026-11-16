package giuliacrepaldi.BStudent_Management.repositories.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectSkillsRepository extends JpaRepository<SubjectSkill, SubjectSkill.SubjectSkillId> {

    // Con la skill già caricata, per mostrarne codice, nome, icona e colore
    @Query("""
            SELECT s FROM SubjectSkill s JOIN FETCH s.skill
            WHERE s.id.subjectId = :subjectId
            ORDER BY s.sortOrder
            """)
    List<SubjectSkill> findBySubjectOrdered(@Param("subjectId") Long subjectId);

    boolean existsById_SubjectIdAndId_SkillId(Long subjectId, Long skillId);

    // 0 se la materia non ha skill: la nuova va in coda con max + 1
    @Query("SELECT COALESCE(MAX(s.sortOrder), 0) FROM SubjectSkill s WHERE s.id.subjectId = :subjectId")
    int findMaxSortOrder(@Param("subjectId") Long subjectId);

    // Voci della Palestra della materia (tramite l'argomento) che usano la skill
    @Query(value = """
            SELECT count(*) FROM practice_exercises pe JOIN topics t ON t.topic_id = pe.topic_id
            WHERE t.subject_id = :subjectId AND pe.skill_id = :skillId
            """, nativeQuery = true)
    long countPracticeExercisesUsing(@Param("subjectId") Long subjectId, @Param("skillId") Long skillId);

    // Pesi degli esercizi nelle missioni della materia che usano la skill
    @Query(value = """
            SELECT count(*) FROM mission_exercise_skills mes
            JOIN mission_exercises me ON me.mission_exercise_id = mes.mission_exercise_id
            JOIN missions m ON m.mission_id = me.mission_id
            JOIN courses c ON c.course_id = m.course_id
            WHERE c.subject_id = :subjectId AND mes.skill_id = :skillId
            """, nativeQuery = true)
    long countMissionWeightsUsing(@Param("subjectId") Long subjectId, @Param("skillId") Long skillId);
}
