package giuliacrepaldi.BStudent_Management.repositories.notebook;

import giuliacrepaldi.BStudent_Management.entities.notebook.ResourceBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceBookmarksRepository extends JpaRepository<ResourceBookmark, ResourceBookmark.ResourceBookmarkId> {

    List<ResourceBookmark> findById_StudentIdOrderBySavedAtDesc(Long studentId);
}
