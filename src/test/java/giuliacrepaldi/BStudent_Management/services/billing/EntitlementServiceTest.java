package giuliacrepaldi.BStudent_Management.services.billing;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.enums.billing.SubscriptionStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.repositories.billing.AccessGrantsRepository;
import giuliacrepaldi.BStudent_Management.repositories.billing.SubscriptionStudentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EntitlementServiceTest {

    private static final Long STUDENT = 7L;

    private SubscriptionStudentsRepository subscriptionStudents;
    private AccessGrantsRepository accessGrants;
    private EntitlementService service;

    @BeforeEach
    void setUp() {
        subscriptionStudents = mock(SubscriptionStudentsRepository.class);
        accessGrants = mock(AccessGrantsRepository.class);
        service = new EntitlementService(subscriptionStudents, accessGrants);
    }

    @Test
    void unCorsoFreeEAccessibileSenzaAbbonamento() {
        assertThat(service.canAccess(STUDENT, course(AccessTier.free))).isTrue();
        verifyNoInteractions(subscriptionStudents, accessGrants);
    }

    @Test
    void unCorsoPremiumRichiedeAbbonamentoOAccessGrant() {
        assertThat(service.canAccess(STUDENT, course(AccessTier.premium))).isFalse();

        when(accessGrants.hasActiveGrant(eq(STUDENT), any())).thenReturn(true);
        assertThat(service.canAccess(STUDENT, course(AccessTier.premium))).isTrue();
    }

    @Test
    void lAbbonamentoContaSoloNegliStatiCheDannoAccesso() {
        when(subscriptionStudents.isCovered(eq(STUDENT), anyCollection(), any())).thenAnswer(invocation -> {
            Collection<SubscriptionStatus> statuses = invocation.getArgument(1);
            return statuses.contains(SubscriptionStatus.active)
                    && statuses.contains(SubscriptionStatus.past_due)
                    && !statuses.contains(SubscriptionStatus.canceled)
                    && !statuses.contains(SubscriptionStatus.incomplete);
        });
        assertThat(service.hasPremium(STUDENT)).isTrue();
    }

    @Test
    void laMissioneSovrascriveIlLivelloDelCorso() {
        Mission freeInPremiumCourse = Mission.builder()
                .accessTier(AccessTier.free).course(course(AccessTier.premium)).build();
        Mission inherited = Mission.builder().course(course(AccessTier.premium)).build();
        Mission premiumInFreeCourse = Mission.builder()
                .accessTier(AccessTier.premium).course(course(AccessTier.free)).build();

        assertThat(service.canAccess(STUDENT, freeInPremiumCourse)).isTrue();
        assertThat(service.canAccess(STUDENT, inherited)).isFalse();
        assertThat(service.canAccess(STUDENT, premiumInFreeCourse)).isFalse();
    }

    @Test
    void ilCorsoNuovoNascePremium() {
        assertThat(Course.builder().build().getAccessTier()).isEqualTo(AccessTier.premium);
    }

    private static Course course(AccessTier tier) {
        return Course.builder().accessTier(tier).build();
    }
}
