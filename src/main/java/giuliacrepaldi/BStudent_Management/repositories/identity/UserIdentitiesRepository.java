package giuliacrepaldi.BStudent_Management.repositories.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.UserIdentity;
import giuliacrepaldi.BStudent_Management.enums.identity.AuthProvider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserIdentitiesRepository extends JpaRepository<UserIdentity, Long> {

    // Login con Google: dall'identità all'account, con i ruoli per il JWT
    @EntityGraph(attributePaths = {"user", "user.roles"})
    Optional<UserIdentity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    boolean existsByUserUserIdAndProvider(Long userId, AuthProvider provider);
}
