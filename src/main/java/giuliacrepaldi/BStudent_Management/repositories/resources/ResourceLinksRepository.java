package giuliacrepaldi.BStudent_Management.repositories.resources;

import giuliacrepaldi.BStudent_Management.entities.resources.ResourceLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// "Approfondisci": nessun uso nell'MVP, pronto per la gestione dei collegamenti dopo la beta
@Repository
public interface ResourceLinksRepository extends JpaRepository<ResourceLink, Long> {
}
