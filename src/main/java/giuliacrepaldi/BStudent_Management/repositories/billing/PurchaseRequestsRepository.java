package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.PurchaseRequest;
import giuliacrepaldi.BStudent_Management.enums.billing.PurchaseRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseRequestsRepository extends JpaRepository<PurchaseRequest, Long> {

    Optional<PurchaseRequest> findByTokenHash(String tokenHash);

    // Blocca la riga: un doppio click sul link attende e poi trova la richiesta già risolta
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM PurchaseRequest r WHERE r.tokenHash = :tokenHash")
    Optional<PurchaseRequest> findForUpdateByTokenHash(@Param("tokenHash") String tokenHash);

    // Una sola richiesta aperta per studente (uq_open_purchase_requests_student)
    Optional<PurchaseRequest> findByStudentStudentIdAndStatus(Long studentId, PurchaseRequestStatus status);
}
