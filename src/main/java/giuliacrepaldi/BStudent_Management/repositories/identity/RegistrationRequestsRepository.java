package giuliacrepaldi.BStudent_Management.repositories.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.RegistrationRequest;
import giuliacrepaldi.BStudent_Management.enums.identity.RegistrationRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface RegistrationRequestsRepository extends JpaRepository<RegistrationRequest, Long> {

    Optional<RegistrationRequest> findByTokenHash(String tokenHash);

    // Blocca la riga: un doppio click sul link attende e poi non trova più la richiesta
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RegistrationRequest r WHERE r.tokenHash = :tokenHash")
    Optional<RegistrationRequest> findForUpdateByTokenHash(@Param("tokenHash") String tokenHash);

    Optional<RegistrationRequest> findByEmailNormalizedAndStatus(String emailNormalized, RegistrationRequestStatus status);

    Optional<RegistrationRequest> findByGoogleSubjectAndStatus(String googleSubject, RegistrationRequestStatus status);

    boolean existsByGoogleSubjectAndStatusAndExpiresAtAfter(String googleSubject, RegistrationRequestStatus status,
                                                           OffsetDateTime now);

    boolean existsByUsernameAndStatusAndEmailNormalizedNot(String username, RegistrationRequestStatus status,
                                                          String emailNormalized);

    @Modifying
    @Query("DELETE FROM RegistrationRequest r WHERE r.expiresAt < :now")
    int deleteExpired(@Param("now") OffsetDateTime now);
}
