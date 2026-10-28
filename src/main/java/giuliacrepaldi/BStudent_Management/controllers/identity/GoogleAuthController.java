package giuliacrepaldi.BStudent_Management.controllers.identity;

import giuliacrepaldi.BStudent_Management.exceptions.UnauthorizedException;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleOnboardingDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleSignupDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleSignupOutcome;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoggedUserDTO;
import giuliacrepaldi.BStudent_Management.security.CookieUtils;
import giuliacrepaldi.BStudent_Management.security.TokenTools;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IGoogleAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Onboarding dopo il ritorno da Google. L'avvio del login è gestito da Spring Security:
// il frontend naviga verso /oauth2/authorization/google
@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private final IGoogleAuthService googleAuthService;
    private final CookieUtils cookieUtils;
    private final TokenTools tokenTools;

    public GoogleAuthController(IGoogleAuthService googleAuthService, CookieUtils cookieUtils, TokenTools tokenTools) {
        this.googleAuthService = googleAuthService;
        this.cookieUtils = cookieUtils;
        this.tokenTools = tokenTools;
    }

    // Precompila il form con email e nome ricevuti da Google
    @GetMapping("/onboarding")
    public GoogleOnboardingDTO onboarding(
            @CookieValue(name = CookieUtils.GOOGLE_ONBOARDING_COOKIE, required = false) String onboardingToken) {
        return googleAuthService.onboardingProfile(requireToken(onboardingToken));
    }

    // 201 + login se dai 14 anni; 202 se sotto i 14 anni (link di consenso inviato al genitore)
    @PostMapping("/complete")
    public ResponseEntity<LoggedUserDTO> complete(
            @CookieValue(name = CookieUtils.GOOGLE_ONBOARDING_COOKIE, required = false) String onboardingToken,
            @RequestBody @Valid GoogleSignupDTO body) {
        GoogleSignupOutcome outcome = googleAuthService.completeGoogleSignup(requireToken(onboardingToken), body);
        String clearOnboarding = cookieUtils.clearGoogleOnboardingCookie().toString();

        return switch (outcome) {
            case GoogleSignupOutcome.Created(var auth) -> ResponseEntity.status(HttpStatus.CREATED)
                    .header(HttpHeaders.SET_COOKIE, clearOnboarding)
                    .header(HttpHeaders.SET_COOKIE, cookieUtils
                            .createAccessTokenCookie(auth.accessToken(), tokenTools.getTokenDuration()).toString())
                    .body(auth.user());
            case GoogleSignupOutcome.AwaitingGuardianConsent() -> ResponseEntity.status(HttpStatus.ACCEPTED)
                    .header(HttpHeaders.SET_COOKIE, clearOnboarding)
                    .build();
        };
    }

    private static String requireToken(String onboardingToken) {
        if (onboardingToken == null || onboardingToken.isBlank()) {
            throw new UnauthorizedException("Registrazione con Google scaduta: accedi di nuovo con Google");
        }
        return onboardingToken;
    }
}
