package giuliacrepaldi.BStudent_Management.services.identity.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.identity.AuthResult;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoggedUserDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoginDTO;

/**
 * Login locale, utente corrente, password e verifica email (blueprint §4).
 * Il JWT viene consegnato dal controller tramite cookie httpOnly (CookieUtils), mai nel body.
 * La registrazione e il consenso del genitore/tutore sono in IRegistrationService.
 */
public interface IAuthService {

    //LOGIN
    // Messaggio unico per email inesistente, password errata, account sospeso o senza password locale
    AuthResult login(LoginDTO body);

    LoggedUserDTO getCurrentUser(Long userId);

    //PASSWORD
    // Per gli account senza password locale (solo Google) si usa il flusso di reset
    void changePassword(Long userId, String currentPassword, String newPassword);

    // Risposta identica che l'email esista o no; vale anche per impostare la prima password locale
    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    //VERIFICA EMAIL (es. studente sotto i 14 anni, il cui link di consenso è andato al genitore)
    void requestEmailVerification(Long userId);

    void verifyEmail(String token);

    //LOGOUT
    //stateless: l'endpoint /auth/logout risponde con CookieUtils.clearAccessTokenCookie(), nessun metodo di service
}
