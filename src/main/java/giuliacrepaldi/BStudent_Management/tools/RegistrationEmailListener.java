package giuliacrepaldi.BStudent_Management.tools;

import giuliacrepaldi.BStudent_Management.services.identity.events.RegistrationCompleted;
import giuliacrepaldi.BStudent_Management.services.identity.events.RegistrationConfirmationRequested;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Le email partono solo dopo il commit: nessun link per richieste mai salvate
// e nessun benvenuto per account annullati da un rollback
@Component
public class RegistrationEmailListener {

    private final EmailSender emailSender;

    public RegistrationEmailListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onConfirmationRequested(RegistrationConfirmationRequested event) {
        if (event.guardianConsent()) {
            emailSender.sendGuardianConsentRequest(event.recipientEmail(), event.studentName(),
                    event.rawToken(), event.expiresAt());
        } else {
            emailSender.sendEmailVerification(event.recipientEmail(), event.studentName(),
                    event.rawToken(), event.expiresAt());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRegistrationCompleted(RegistrationCompleted event) {
        emailSender.sendWelcomeEmail(event.studentEmail(), event.studentName());
        if (event.guardianEmail() != null) {
            emailSender.sendGuardianConsentConfirmed(event.guardianEmail(), event.studentName());
        }
    }
}
