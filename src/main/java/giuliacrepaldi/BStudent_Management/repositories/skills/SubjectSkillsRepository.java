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

    // Skill della materia che contano per la media dei livelli: hanno almeno un esercizio pubblicato,
    // in Palestra o in una missione (Blueprint Gamification §4.2)
    @Query(value = """
            SELECT ss.skill_id FROM subject_skills ss
            WHERE ss.subject_id = :subjectId
              AND (EXISTS (SELECT 1 FROM practice_exercises pe
                             JOIN topics t ON t.topic_id = pe.topic_id
                             JOIN question_bank_entries q ON q.question_bank_entry_id = pe.question_bank_entry_id
                            WHERE t.subject_id = ss.subject_id AND pe.skill_id = ss.skill_id
                              AND pe.status = 'published' AND q.status <> 'withdrawn')
                OR EXISTS (SELECT 1 FROM mission_exercise_skills mes
                             JOIN mission_exercises me ON me.mission_exercise_id = mes.mission_exercise_id
                             JOIN missions m ON m.mission_id = me.mission_id
                             JOIN courses c ON c.course_id = m.course_id
                            WHERE c.subject_id = ss.subject_id AND mes.skill_id = ss.skill_id
                              AND me.status = 'published' AND m.status = 'published' AND c.status = 'published'))
            """, nativeQuery = true)
    List<Long> findSkillIdsWithPublishedExercises(@Param("subjectId") Long subjectId);

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
