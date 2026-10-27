package giuliacrepaldi.BStudent_Management.tools;

import giuliacrepaldi.BStudent_Management.services.identity.events.EmailVerificationRequested;
import giuliacrepaldi.BStudent_Management.services.identity.events.PasswordChanged;
import giuliacrepaldi.BStudent_Management.services.identity.events.PasswordResetRequested;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Le email partono solo dopo il commit: nessun link per token mai salvati
@Component
public class AuthEmailListener {

    private final EmailSender emailSender;

    public AuthEmailListener(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequested event) {
        emailSender.sendPasswordReset(event.email(), event.rawToken(), event.expiresAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordChanged(PasswordChanged event) {
        emailSender.sendPasswordChanged(event.email());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailVerificationRequested(EmailVerificationRequested event) {
        emailSender.sendAccountEmailVerification(event.email(), event.name(), event.rawToken(), event.expiresAt());
    }
}
