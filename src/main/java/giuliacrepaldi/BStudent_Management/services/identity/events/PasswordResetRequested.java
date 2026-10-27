package giuliacrepaldi.BStudent_Management.services.identity.events;

import java.time.OffsetDateTime;

public record PasswordResetRequested(String email, String rawToken, OffsetDateTime expiresAt) {

    // Il token in chiaro non deve finire nei log
    @Override
    public String toString() {
        return "PasswordResetRequested[]";
    }
}
