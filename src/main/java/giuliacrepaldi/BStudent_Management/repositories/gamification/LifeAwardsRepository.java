package giuliacrepaldi.BStudent_Management.repositories.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.LifeAward;
import giuliacrepaldi.BStudent_Management.enums.gamification.LifeAwardReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface LifeAwardsRepository extends JpaRepository<LifeAward, Long> {

    boolean existsByEventId(String eventId);

    boolean existsByStudentIdAndReasonAndAwardDay(Long studentId, LifeAwardReason reason, LocalDate awardDay);
}
