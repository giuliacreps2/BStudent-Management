package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.SubscriptionStudent;
import giuliacrepaldi.BStudent_Management.enums.billing.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionStudentsRepository extends JpaRepository<SubscriptionStudent, Long> {

    // Lo studente è coperto da un abbonamento in uno degli stati indicati e non ancora scaduto
    @Query("""
            SELECT COUNT(ss) > 0 FROM SubscriptionStudent ss JOIN ss.subscription s
            WHERE ss.student.studentId = :studentId AND ss.removedAt IS NULL
              AND s.status IN :statuses AND s.currentPeriodEnd > :now""")
    boolean isCovered(@Param("studentId") Long studentId,
                      @Param("statuses") Collection<SubscriptionStatus> statuses,
                      @Param("now") OffsetDateTime now);

    // Copertura aperta (al massimo una, per l'indice uq_open_subscription_students_student)
    Optional<SubscriptionStudent> findByStudentStudentIdAndRemovedAtIsNull(Long studentId);

    // Studenti attualmente nel piano: il service li confronta con plan.maxStudents
    List<SubscriptionStudent> findBySubscriptionSubscriptionIdAndRemovedAtIsNull(Long subscriptionId);

    long countBySubscriptionSubscriptionIdAndRemovedAtIsNull(Long subscriptionId);
}
