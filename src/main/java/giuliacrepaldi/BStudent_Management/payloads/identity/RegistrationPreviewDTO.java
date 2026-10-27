package giuliacrepaldi.BStudent_Management.payloads.identity;

import java.time.OffsetDateTime;

// Cosa mostra la pagina del link: nessun dato oltre a quelli necessari per decidere
public record RegistrationPreviewDTO(
        String studentName,
        String studentUsername,
        boolean requiresGuardianConsent,
        // Il frontend sceglie se chiedere il login o la creazione dell'account del genitore
        boolean guardianHasAccount,
        String policyVersion,
        OffsetDateTime expiresAt) {
}
