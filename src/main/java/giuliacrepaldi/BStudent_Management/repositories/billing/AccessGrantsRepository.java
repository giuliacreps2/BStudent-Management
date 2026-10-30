package giuliacrepaldi.BStudent_Management.repositories.billing;

import giuliacrepaldi.BStudent_Management.entities.billing.AccessGrant;
import giuliacrepaldi.BStudent_Management.enums.billing.AccessGrantSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AccessGrantsRepository extends JpaRepository<AccessGrant, Long> {

    // Accesso premium in corso: iniziato, non scaduto, non revocato
    @Query("""
            SELECT COUNT(g) > 0 FROM AccessGrant g
            WHERE g.student.studentId = :studentId AND g.revokedAt IS NULL
              AND g.startsAt <= :now AND (g.endsAt IS NULL OR g.endsAt > :now)""")
    boolean hasActiveGrant(@Param("studentId") Long studentId, @Param("now") OffsetDateTime now);

    // Diritto allo sconto beta: basta aver avuto un accesso beta, anche se già scaduto
    boolean existsByStudentStudentIdAndSource(Long studentId, AccessGrantSource source);

    // Avvisi di fine beta (es. 30 giorni prima)
    @Query("""
            SELECT g FROM AccessGrant g JOIN FETCH g.student s JOIN FETCH s.user
            WHERE g.source = :source AND g.revokedAt IS NULL AND g.endsAt BETWEEN :from AND :to""")
    List<AccessGrant> findEndingBetween(@Param("source") AccessGrantSource source,
                                        @Param("from") OffsetDateTime from,
                                        @Param("to") OffsetDateTime to);
}
