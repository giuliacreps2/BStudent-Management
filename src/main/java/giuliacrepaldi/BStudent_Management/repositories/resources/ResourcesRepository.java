package giuliacrepaldi.BStudent_Management.repositories.resources;

import java.util.List;
import java.util.Optional;
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

    // Risorsa da ripassare per un argomento: la prima pubblicata (Blueprint Gamification §6.2)
    Optional<Resource> findFirstByTopic_TopicIdAndStatusOrderByResourceId(Long topicId, ContentStatus status);

    // Risorse che lo studente vede nella materia
    List<Resource> findBySubject_SubjectIdAndStatusOrderByTitle(Long subjectId, ContentStatus status);

    Optional<Resource> findBySubject_SubjectIdAndSlug(Long subjectId, String slug);

    // Ricerca semplice per titolo e descrizione (blueprint §7.4)
    @Query("""
            SELECT r FROM Resource r
            WHERE r.subject.subjectId = :subjectId
              AND r.status = giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus.published
              AND (LOWER(r.title) LIKE LOWER(CONCAT('%', :text, '%'))
                   OR LOWER(r.description) LIKE LOWER(CONCAT('%', :text, '%')))
            ORDER BY r.title
            """)
    List<Resource> searchVisible(@Param("subjectId") Long subjectId, @Param("text") String text, Pageable pageable);
}
