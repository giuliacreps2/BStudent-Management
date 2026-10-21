package giuliacrepaldi.BStudent_Management.tools;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailSender {

    @Value("${resend.api-key}")
    private String apiKey;


    //REGISTRATION EMAIL
    public void sendRegistrationEmail(RegistrationRequest req) {
        try {
            Resend resend = new Resend(apiKey);

            String verifyUrl = "http://localhost:{PORT}/verify?token=" + req.getVerificationToken();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("")
                    .to(req.getEmail())
                    .subject("")
                    .html("""
                            
                            """.formatted(verifyUrl))
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email send successfully: '{}','{}'", req.getEmail(), response.getId());

        } catch (ResendException e) {
            log.error("Email send failed for {}: {}", req.getEmail(), e.getMessage());
        }
    }

    //WELCOME NEW USER
    public void sendWelcomeEmail(User user) {
        try {
            Resend resend = new Resend(apiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("")
                    .to(user.getEmail())
                    .subject("Identità digitale creata con successo!")
                    .html("""
                            
                            """.formatted(user.getEmail()))
                    .build();
            CreateEmailResponse response = resend.emails().send(params);
            log.info("Welcome email sent successfully to {}: {}", user.getEmail(), response.getId());
        } catch (ResendException e) {
            log.error("Welcome email failed for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    //MAIL NUOVO UTENTE REGISTRATO
    public void sendRegistrationEmailAdmin(RegistrationRequest req) {
        try {
            Resend resend = new Resend(apiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("")
                    .to("")
                    .subject("Nuovo utente registrato: " + req.getEmail())
                    .html("""
                            
                            """.formatted(req.getEmail(), req.getClientCategory()))
                    .build();
            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email to admin send successful for {}: {}", req.getEmail(), response.getId());

        } catch (ResendException e) {
            log.error("Email to admin send failed for {}: {}", req.getEmail(), e.getMessage());
        }
    }

    //RESET PASSWORD
    public void sendNewPassword(PasswordResetToken newPassword) {
        try {
            Resend resend = new Resend(apiKey);

            String verifyUrl = "http://localhost:{PORT}/reset-password?token=" + newPassword.getResetToken();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("")
                    .to(newPassword.getUser().getEmail())
                    .subject("Compila la tua nuova password")
                    .html("""
                            
                            """.formatted(verifyUrl))
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email to reset old password send successfully: '{}','{}'", newPassword.getUser().getEmail(), response.getId());

        } catch (ResendException e) {
            log.error("Email to reset old send failed for {}: {}", newPassword.getUser().getEmail(), e.getMessage());
        }

    }

    public void confirmNewPassword(PasswordResetToken found) {
        try {
            Resend resend = new Resend(apiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("")
                    .to(found.getUser().getEmail())
                    .subject("La tua password è stata modificata")
                    .html("""
                            
                            """)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email conferma cambio password inviata: '{}','{}'", found.getUser().getEmail(), response.getId());

        } catch (ResendException e) {
            log.error("Invio email conferma cambio password fallito per {}: {}", found.getUser().getEmail(), e.getMessage());
        }
    }
}
