package giuliacrepaldi.BStudent_Management.services.identity.events;

import java.time.OffsetDateTime;

// Pubblicato quando serve inviare (o reinviare) il link di conferma o di consenso
public record RegistrationConfirmationRequested(
        String recipientEmail,
        String studentName,
        String rawToken,
        boolean guardianConsent,
        OffsetDateTime expiresAt) {

    // Il token in chiaro non deve finire nei log
    @Override
    public String toString() {
        return "RegistrationConfirmationRequested[guardianConsent=" + guardianConsent + "]";
    }
}
