package giuliacrepaldi.BStudent_Management.repositories.resources;

import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.resources.ResourceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourcesRepository extends JpaRepository<Resource, Long> {

    boolean existsBySubject_SubjectIdAndSlug(Long subjectId, String slug);

    boolean existsBySubject_SubjectIdAndSlugAndResourceIdNot(Long subjectId, String slug, Long resourceId);

    // Filtri dell'Admin, tutti facoltativi (null = nessun filtro)
    @Query("""
            SELECT r FROM Resource r
            WHERE (:subjectId IS NULL OR r.subject.subjectId = :subjectId)
              AND (:category IS NULL OR r.category = :category)
              AND (:status IS NULL OR r.status = :status)
            """)
    Page<Resource> search(@Param("subjectId") Long subjectId, @Param("category") ResourceCategory category,
                          @Param("status") ContentStatus status, Pageable pageable);
}
