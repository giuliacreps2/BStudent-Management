package giuliacrepaldi.BStudent_Management.repositories.progression;

import giuliacrepaldi.BStudent_Management.enums.progression.SourceKind;
import java.math.BigDecimal;
import giuliacrepaldi.BStudent_Management.entities.progression.SkillCreditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillCreditEntriesRepository extends JpaRepository<SkillCreditEntry, Long> {

    // Punti veri dello studente per materia e skill (Palestra + missioni), dalla vista
    // student_subject_skill_totals (v0.10). Righe: subject_id, skill_id, punti.
    // Gli accrediti senza materia non contano per i livelli
    @Query(value = """
            SELECT subject_id, skill_id, sum(points)
              FROM student_subject_skill_totals
             WHERE student_id = :studentId AND subject_id IS NOT NULL
             GROUP BY subject_id, skill_id
            """, nativeQuery = true)
    List<Object[]> findTotalsByStudent(@Param("studentId") Long studentId);

    // Idempotenza: un evento accredita una skill al massimo una volta
    boolean existsByEventIdAndSkill_SkillId(String eventId, Long skillId);

    // Migliore contributo già registrato per studente, skill e origine (voce della Palestra oppure
    // esercizio della missione): 0 se è la prima volta
    @Query("""
            SELECT COALESCE(MAX(e.newContribution), 0) FROM SkillCreditEntry e
            WHERE e.student.studentId = :studentId AND e.skill.skillId = :skillId
              AND e.sourceKind = :sourceKind AND e.sourceId = :sourceId
            """)
    BigDecimal findBestContribution(@Param("studentId") Long studentId, @Param("skillId") Long skillId,
                                    @Param("sourceKind") SourceKind sourceKind, @Param("sourceId") Long sourceId);
}
