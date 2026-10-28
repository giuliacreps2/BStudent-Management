package giuliacrepaldi.BStudent_Management.services.identity.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleLoginOutcome;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleOnboardingDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleProfile;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleSignupDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.GoogleSignupOutcome;

/**
 * Accesso e registrazione con Google OAuth2/OIDC ("Google as Gateway").
 * Login: identità già collegata in user_identities, oppure collegamento automatico all'account con la
 * stessa email, solo se Google la dichiara verificata (vale anche per i minori già registrati: nessun nuovo consenso).
 * Nuovo utente: Google non fornisce l'età, quindi un passaggio di onboarding chiede data di nascita e username.
 * Dai 14 anni l'account nasce subito; sotto i 14 anni parte il consenso del genitore/tutore via email.
 */
public interface IGoogleAuthService {

    // Chiamato dal success handler OAuth2 dopo la verifica dell'id_token
    GoogleLoginOutcome handleGoogleLogin(GoogleProfile profile);

    // Dati per precompilare il form di onboarding (il cookie è httpOnly)
    GoogleOnboardingDTO onboardingProfile(String onboardingToken);

    // Secondo passaggio della registrazione: età, username, informativa ed eventuale email del genitore
    GoogleSignupOutcome completeGoogleSignup(String onboardingToken, GoogleSignupDTO body);
}
