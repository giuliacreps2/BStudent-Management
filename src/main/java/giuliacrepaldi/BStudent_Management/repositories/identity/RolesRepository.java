package giuliacrepaldi.BStudent_Management.repositories.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    @Query("SELECT r FROM User u JOIN u.roles r WHERE u.userId = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);
}
