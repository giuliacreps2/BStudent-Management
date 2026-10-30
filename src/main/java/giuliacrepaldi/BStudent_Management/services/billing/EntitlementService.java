package giuliacrepaldi.BStudent_Management.services.billing;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.enums.billing.AccessGrantSource;
import giuliacrepaldi.BStudent_Management.enums.billing.SubscriptionStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.repositories.billing.AccessGrantsRepository;
import giuliacrepaldi.BStudent_Management.repositories.billing.SubscriptionStudentsRepository;
import giuliacrepaldi.BStudent_Management.services.billing.interfaces.IEntitlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Set;

@Service
public class EntitlementService implements IEntitlementService {

    // past_due: rinnovo fallito, Stripe sta ritentando. L'accesso resta fino alla fine del periodo;
    // se i tentativi falliscono Stripe chiude l'abbonamento (canceled/unpaid) e l'accesso termina
    private static final Set<SubscriptionStatus> PREMIUM_STATUSES =
            EnumSet.of(SubscriptionStatus.trialing, SubscriptionStatus.active, SubscriptionStatus.past_due);

    private final SubscriptionStudentsRepository subscriptionStudentsRepository;
    private final AccessGrantsRepository accessGrantsRepository;

    public EntitlementService(SubscriptionStudentsRepository subscriptionStudentsRepository,
                              AccessGrantsRepository accessGrantsRepository) {
        this.subscriptionStudentsRepository = subscriptionStudentsRepository;
        this.accessGrantsRepository = accessGrantsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPremium(Long studentId) {
        OffsetDateTime now = OffsetDateTime.now();
        return accessGrantsRepository.hasActiveGrant(studentId, now)
                || subscriptionStudentsRepository.isCovered(studentId, PREMIUM_STATUSES, now);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccess(Long studentId, Course course) {
        return course.getAccessTier() == AccessTier.free || hasPremium(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccess(Long studentId, Mission mission) {
        AccessTier tier = mission.getAccessTier() != null
                ? mission.getAccessTier()
                : mission.getCourse().getAccessTier();
        return tier == AccessTier.free || hasPremium(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBetaUser(Long studentId) {
        return accessGrantsRepository.existsByStudentStudentIdAndSource(studentId, AccessGrantSource.beta);
    }
}
