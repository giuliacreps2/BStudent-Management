package giuliacrepaldi.BStudent_Management.repositories.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;
import java.util.Collection;
import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionAttemptsRepository extends JpaRepository<MissionAttempt, Long> {

    // Tentativo di missione in corso (attivo, salvato o in correzione)
    boolean existsByStudent_StudentIdAndStatusIn(Long studentId, Collection<MissionAttemptStatus> statuses);
}
