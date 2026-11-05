package giuliacrepaldi.BStudent_Management.repositories.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursesRepository extends JpaRepository<Course, Long> {

    Page<Course> findBySubject_SubjectId(Long subjectId, Pageable pageable);

    boolean existsBySubject_SubjectId(Long subjectId);
}
