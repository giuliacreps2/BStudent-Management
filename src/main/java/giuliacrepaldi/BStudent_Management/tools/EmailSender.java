package giuliacrepaldi.BStudent_Management.tools;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// I metodi ricevono il token in chiaro: nel DB se ne conserva solo l'hash
@Slf4j
@Component
public class EmailSender {

    private static final DateTimeFormatter EXPIRY_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm").withZone(ZoneId.of("Europe/Rome"));

    private final Resend resend;
    private final String from;
    private final String frontendUrl;

    public EmailSender(Resend resend,
                       @Value("${resend.from}") String from,
                       @Value("${app.frontend-url}") String frontendUrl) {
        this.resend = resend;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    //REGISTRATION: verifica email dello studente (14 anni o più)
    public void sendEmailVerification(String to, String studentName, String rawToken, OffsetDateTime expiresAt) {
        String url = frontendUrl + "/registration/confirm?token=" + rawToken;
        send(to, "Conferma la tua registrazione a BStudent", """
                <p>Ciao %s,</p>
                <p>per completare la registrazione a BStudent conferma il tuo indirizzo email:</p>
                <p><a href="%s">Conferma la registrazione</a></p>
                <p>Il link scade il %s. Se non hai richiesto la registrazione, ignora questa email:
                i dati inseriti verranno cancellati automaticamente.</p>
                """.formatted(escape(studentName), url, EXPIRY_FORMAT.format(expiresAt)));
    }

    //REGISTRATION: richiesta di consenso al genitore/tutore (studente sotto i 14 anni)
    public void sendGuardianConsentRequest(String to, String studentName, String rawToken, OffsetDateTime expiresAt) {
        String url = frontendUrl + "/registration/guardian-consent?token=" + rawToken;
        send(to, "Richiesta di autorizzazione per la registrazione a BStudent", """
                <p>Gentile genitore/tutore,</p>
                <p>%s ha richiesto la registrazione a BStudent indicando questo indirizzo email
                come riferimento del genitore o tutore.</p>
                <p>Poiché ha meno di 14 anni, la registrazione può essere completata solo con il tuo consenso.
                Dalla pagina seguente puoi leggere l'informativa, autorizzare o rifiutare la richiesta:</p>
                <p><a href="%s">Gestisci la richiesta</a></p>
                <p>Il link scade il %s. Senza una risposta, i dati inseriti verranno cancellati automaticamente.</p>
                """.formatted(escape(studentName), url, EXPIRY_FORMAT.format(expiresAt)));
    }

    //WELCOME NEW USER
    public void sendWelcomeEmail(String to, String name) {
        send(to, "Benvenuto in BStudent!", """
                <p>Ciao %s,</p>
                <p>il tuo account BStudent è attivo. Buono studio!</p>
                """.formatted(escape(name)));
    }

    //CONSENSO REGISTRATO: conferma al genitore/tutore
    public void sendGuardianConsentConfirmed(String to, String studentName) {
        send(to, "Consenso registrato", """
                <p>Gentile genitore/tutore,</p>
                <p>il tuo consenso è stato registrato e l'account di %s è ora attivo.
                Puoi gestire il collegamento dal tuo account BStudent.</p>
                """.formatted(escape(studentName)));
    }

    //RESET PASSWORD
    // Vale anche per gli account solo Google, che così impostano una password locale
    public void sendPasswordReset(String to, String rawToken, OffsetDateTime expiresAt) {
        String url = frontendUrl + "/reset-password?token=" + rawToken;
        send(to, "Imposta la tua nuova password", """
                <p>Abbiamo ricevuto una richiesta di impostazione della password.</p>
                <p><a href="%s">Imposta una nuova password</a></p>
                <p>Il link scade il %s. Se non sei stato tu, ignora questa email.</p>
                """.formatted(url, EXPIRY_FORMAT.format(expiresAt)));
    }

    public void sendPasswordChanged(String to) {
        send(to, "La tua password è stata modificata", """
                <p>La password del tuo account BStudent è stata modificata.</p>
                <p>Se non sei stato tu, contatta subito l'assistenza.</p>
                """);
    }

    //VERIFICA EMAIL di un account esistente (es. studente sotto i 14 anni dopo il consenso)
    public void sendAccountEmailVerification(String to, String name, String rawToken, OffsetDateTime expiresAt) {
        String url = frontendUrl + "/verify-email?token=" + rawToken;
        send(to, "Verifica il tuo indirizzo email", """
                <p>Ciao %s,</p>
                <p>conferma che questo indirizzo email appartiene al tuo account BStudent:</p>
                <p><a href="%s">Verifica l'email</a></p>
                <p>Il link scade il %s. Se non hai un account BStudent, ignora questa email.</p>
                """.formatted(escape(name), url, EXPIRY_FORMAT.format(expiresAt)));
    }

    private void send(String to, String subject, String html) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email '{}' inviata, id Resend {}", subject, response.getId());
        } catch (ResendException e) {
            // Nessun indirizzo nei log: è un dato personale (anche di minori)
            log.error("Invio email '{}' fallito: {}", subject, e.getMessage());
        }
    }

    private static String escape(String value) {
        return value == null ? "" : HtmlUtils.htmlEscape(value);
    }
}
