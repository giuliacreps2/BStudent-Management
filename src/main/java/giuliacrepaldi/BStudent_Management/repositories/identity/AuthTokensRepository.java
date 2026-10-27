package giuliacrepaldi.BStudent_Management.repositories.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.AuthToken;
import giuliacrepaldi.BStudent_Management.enums.identity.TokenPurpose;
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
public interface AuthTokensRepository extends JpaRepository<AuthToken, Long> {

    // Blocca la riga: lo stesso link usato due volte in parallelo viene consumato una volta sola
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM AuthToken t JOIN FETCH t.user WHERE t.tokenHash = :tokenHash")
    Optional<AuthToken> findForUpdateByTokenHash(@Param("tokenHash") String tokenHash);

    boolean existsByUserUserIdAndPurposeAndCreatedAtAfter(Long userId, TokenPurpose purpose, OffsetDateTime after);

    // Un nuovo link invalida quelli ancora aperti per lo stesso scopo
    @Modifying
    @Query("""
            UPDATE AuthToken t SET t.revokedAt = :now
            WHERE t.user.userId = :userId AND t.purpose = :purpose
              AND t.consumedAt IS NULL AND t.revokedAt IS NULL
            """)
    int revokeOpenTokens(@Param("userId") Long userId, @Param("purpose") TokenPurpose purpose,
                         @Param("now") OffsetDateTime now);
}
