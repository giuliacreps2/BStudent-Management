package giuliacrepaldi.BStudent_Management.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtils {

    private static final String COOKIE_NAME = "accessToken";
    private static final String COOKIE_PATH = "/";

    // Inviato solo agli endpoint che completano la registrazione con Google
    public static final String GOOGLE_ONBOARDING_COOKIE = "googleOnboarding";
    private static final String GOOGLE_ONBOARDING_PATH = "/auth/google";

    //TODO da mettere true in fase di produzione in app.properties
    @Value("${cookie.secure:false}")
    private boolean secure;

    public ResponseCookie createAccessTokenCookie(String token, Duration maxAge) {
        return build(COOKIE_NAME, token, COOKIE_PATH, maxAge);
    }

    public ResponseCookie clearAccessTokenCookie() {
        return build(COOKIE_NAME, "", COOKIE_PATH, Duration.ZERO);
    }

    public ResponseCookie createGoogleOnboardingCookie(String token, Duration maxAge) {
        return build(GOOGLE_ONBOARDING_COOKIE, token, GOOGLE_ONBOARDING_PATH, maxAge);
    }

    public ResponseCookie clearGoogleOnboardingCookie() {
        return build(GOOGLE_ONBOARDING_COOKIE, "", GOOGLE_ONBOARDING_PATH, Duration.ZERO);
    }

    // Lax: il cookie viaggia anche sul redirect di ritorno da Google (navigazione GET di primo livello)
    public ResponseCookie build(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
    }
}
