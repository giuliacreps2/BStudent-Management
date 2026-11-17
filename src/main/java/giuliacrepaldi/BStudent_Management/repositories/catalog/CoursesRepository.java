package giuliacrepaldi.BStudent_Management.repositories.catalog;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import java.util.List;
import java.util.Optional;
import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursesRepository extends JpaRepository<Course, Long> {

    Page<Course> findBySubject_SubjectId(Long subjectId, Pageable pageable);

    boolean existsBySubject_SubjectId(Long subjectId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndCourseIdNot(String slug, Long courseId);

    // Corso d'ingresso della materia: al massimo uno (Blueprint Gamification §8-bis)
    Optional<Course> findBySubject_SubjectIdAndEntryCourseTrue(Long subjectId);

    Optional<Course> findBySlug(String slug);

    List<Course> findBySubject_SubjectIdAndStatusOrderByCourseId(Long subjectId, ContentStatus status);

    boolean existsBySubject_SubjectIdAndStatus(Long subjectId, ContentStatus status);

    // Skill diverse allenate dagli esercizi che lo studente vede nel corso (blueprint §5.8)
    @Query(value = """
            SELECT count(DISTINCT mes.skill_id)
              FROM mission_exercise_skills mes
              JOIN mission_exercises me ON me.mission_exercise_id = mes.mission_exercise_id
              JOIN question_bank_entries q ON q.question_bank_entry_id = me.question_bank_entry_id
              JOIN missions m ON m.mission_id = me.mission_id
             WHERE m.course_id = :courseId AND m.status = 'published'
               AND me.status = 'published' AND q.status <> 'withdrawn'
            """, nativeQuery = true)
    long countVisibleSkills(@Param("courseId") Long courseId);

    // Le stesse skill, per mostrarne nome, icona e colore
    @Query(value = """
            SELECT DISTINCT mes.skill_id
              FROM mission_exercise_skills mes
              JOIN mission_exercises me ON me.mission_exercise_id = mes.mission_exercise_id
              JOIN question_bank_entries q ON q.question_bank_entry_id = me.question_bank_entry_id
              JOIN missions m ON m.mission_id = me.mission_id
             WHERE m.course_id = :courseId AND m.status = 'published'
               AND me.status = 'published' AND q.status <> 'withdrawn'
            """, nativeQuery = true)
    List<Long> findVisibleSkillIds(@Param("courseId") Long courseId);

    // Esercizi che lo studente vede nel corso: badge della landing
    @Query(value = """
            SELECT count(*)
              FROM mission_exercises me
              JOIN question_bank_entries q ON q.question_bank_entry_id = me.question_bank_entry_id
              JOIN missions m ON m.mission_id = me.mission_id
             WHERE m.course_id = :courseId AND m.status = 'published'
               AND me.status = 'published' AND q.status <> 'withdrawn'
            """, nativeQuery = true)
    long countVisibleExercises(@Param("courseId") Long courseId);
}
