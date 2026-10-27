package giuliacrepaldi.BStudent_Management.repositories.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {

    // Esclude gli utenti cancellati (@SQLRestriction su User)
    Optional<User> findByEmailNormalized(String emailNormalized);

    // Ruoli caricati nella stessa query: il principal resta utilizzabile fuori dalla transazione
    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByUserId(Long userId);

    @EntityGraph(attributePaths = "roles")
    Optional<User> findWithRolesByEmailNormalized(String emailNormalized);

    // Nativa per includere anche gli utenti cancellati: l'unique su email_normalized vale per tutti
    @Query(value = "SELECT EXISTS (SELECT 1 FROM users WHERE email_normalized = :email)", nativeQuery = true)
    boolean existsByEmailIncludingDeleted(@Param("email") String emailNormalized);
}
