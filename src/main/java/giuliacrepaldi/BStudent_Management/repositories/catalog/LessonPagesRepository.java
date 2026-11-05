package giuliacrepaldi.BStudent_Management.repositories.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.LessonPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonPagesRepository extends JpaRepository<LessonPage, Long> {

    List<LessonPage> findByLesson_LessonIdOrderByLessonPageSortOrder(Long lessonId);

    long countByLesson_LessonId(Long lessonId);

    // 0 se la lezione non ha pagine: la nuova va in coda con max + 1
    @Query("SELECT COALESCE(MAX(p.lessonPageSortOrder), 0) FROM LessonPage p WHERE p.lesson.lessonId = :lessonId")
    int findMaxSortOrder(@Param("lessonId") Long lessonId);

    @Modifying
    @Query("DELETE FROM LessonPage p WHERE p.lesson.lessonId = :lessonId")
    void deleteByLessonId(@Param("lessonId") Long lessonId);
}
