package giuliacrepaldi.BStudent_Management.security.oauth2;

import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleLoginOutcome;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleProfile;
import giuliacrepaldi.BStudent_Management.security.CookieUtils;
import giuliacrepaldi.BStudent_Management.security.TokenTools;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IGoogleAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Ritorno da Google con id_token verificato: emette il cookie giusto e rimanda al frontend.
// Nessuna sessione: l'autenticazione OAuth2 serve solo per questa richiesta, poi vale il JWT
@Slf4j
@Component
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final IGoogleAuthService googleAuthService;
    private final CookieUtils cookieUtils;
    private final TokenTools tokenTools;
    private final String frontendUrl;

    public GoogleLoginSuccessHandler(IGoogleAuthService googleAuthService, CookieUtils cookieUtils,
                                     TokenTools tokenTools, @Value("${app.frontend-url}") String frontendUrl) {
        this.googleAuthService = googleAuthService;
        this.cookieUtils = cookieUtils;
        this.tokenTools = tokenTools;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        SecurityContextHolder.clearContext();

        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            log.warn("Login OAuth2 senza utente OIDC: scope openid mancante?");
            response.sendRedirect(frontendUrl + "/login?error=" + GoogleLoginFailureHandler.LOGIN_FAILED);
            return;
        }

        GoogleProfile profile = new GoogleProfile(oidcUser.getSubject(), oidcUser.getEmail(),
                Boolean.TRUE.equals(oidcUser.getEmailVerified()), oidcUser.getGivenName(), oidcUser.getFamilyName());

        String target;
        try {
            target = switch (googleAuthService.handleGoogleLogin(profile)) {
                case GoogleLoginOutcome.LoggedIn(var auth) -> {
                    response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils
                            .createAccessTokenCookie(auth.accessToken(), tokenTools.getTokenDuration()).toString());
                    // Stessa destinazione del login con password (HOME_AFTER_LOGIN nel frontend)
                    yield frontendUrl + "/my/dashboard";
                }
                case GoogleLoginOutcome.OnboardingRequired(var onboardingToken) -> {
                    response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils
                            .createGoogleOnboardingCookie(onboardingToken, tokenTools.getGoogleOnboardingDuration())
                            .toString());
                    yield frontendUrl + "/register/google";
                }
                case GoogleLoginOutcome.Rejected(var reason) -> frontendUrl + "/login?error=" + reason;
            };
        } catch (RuntimeException e) {
            log.error("Errore durante il login con Google", e);
            target = frontendUrl + "/login?error=" + GoogleLoginFailureHandler.LOGIN_FAILED;
        }
        response.sendRedirect(target);
    }
}
