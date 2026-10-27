package giuliacrepaldi.BStudent_Management.services.identity.events;

import java.time.OffsetDateTime;

public record EmailVerificationRequested(String email, String name, String rawToken, OffsetDateTime expiresAt) {

    // Il token in chiaro non deve finire nei log
    @Override
    public String toString() {
        return "EmailVerificationRequested[]";
    }
}
