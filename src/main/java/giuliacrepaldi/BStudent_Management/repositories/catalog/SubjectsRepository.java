package giuliacrepaldi.BStudent_Management.repositories.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import java.util.List;
import java.util.Optional;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectsRepository extends JpaRepository<Subject, Long> {

    boolean existsBySubjectNameIgnoreCase(String subjectName);

    boolean existsBySubjectNameIgnoreCaseAndSubjectIdNot(String subjectName, Long subjectId);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndSubjectIdNot(String slug, Long subjectId);

    Optional<Subject> findBySlug(String slug);

    List<Subject> findByStatusOrderBySubjectId(SubjectStatus status);
}
