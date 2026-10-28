package giuliacrepaldi.BStudent_Management.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Login annullato su Google, state non valido, id_token rifiutato...: rimanda al frontend con un codice generico
@Slf4j
@Component
public class GoogleLoginFailureHandler implements AuthenticationFailureHandler {

    static final String LOGIN_FAILED = "google_login_failed";

    private final String frontendUrl;

    public GoogleLoginFailureHandler(@Value("${app.frontend-url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("Login con Google non riuscito: {}", exception.getMessage());
        response.sendRedirect(frontendUrl + "/login?error=" + LOGIN_FAILED);
    }
}
