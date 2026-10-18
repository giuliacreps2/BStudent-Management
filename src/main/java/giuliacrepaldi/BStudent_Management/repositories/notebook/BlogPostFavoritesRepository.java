package giuliacrepaldi.BStudent_Management.repositories.notebook;

import giuliacrepaldi.BStudent_Management.entities.notebook.BlogPostFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogPostFavoritesRepository extends JpaRepository<BlogPostFavorite, BlogPostFavorite.BlogPostFavoriteId> {
}
