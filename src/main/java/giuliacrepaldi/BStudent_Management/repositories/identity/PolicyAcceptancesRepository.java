package giuliacrepaldi.BStudent_Management.repositories.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.PolicyAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyAcceptancesRepository extends JpaRepository<PolicyAcceptance, Long> {

    boolean existsByUserUserIdAndDocumentCodeAndDocumentVersion(Long userId, String documentCode, String documentVersion);
}
