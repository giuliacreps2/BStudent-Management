package giuliacrepaldi.BStudent_Management.services.identity.interfaces;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.identity.GuardianConsentDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.RegisterAccountUserDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.RegistrationPreviewDTO;

/**
 * Registrazione degli studenti con conferma email o consenso del genitore/tutore (blueprint §4).
 * I dati restano in registration_requests finché non arriva la conferma: solo allora nascono users e students.
 * Dai 14 anni lo studente conferma da solo la propria email; sotto i 14 anni serve il consenso
 * del genitore/tutore (GDPR art. 8, art. 2-quinquies Codice privacy), che deve avere o creare un account.
 * Richieste rifiutate o scadute vengono cancellate definitivamente.
 */
public interface IRegistrationService {

    // Risposta identica che l'email sia già registrata o no: non rivela chi ha un account
    void register(RegisterAccountUserDTO body);

    // Dati mostrati dalla pagina aperta dal link (conferma o consenso)
    RegistrationPreviewDTO preview(String token);

    // Dai 14 anni: lo studente conferma la propria email e l'account viene creato
    User confirmEmail(String token);

    // Sotto i 14 anni: il genitore/tutore autorizza. Se ha già un account deve essere autenticato
    // (currentUser); altrimenti l'account viene creato con la password indicata nel body
    User confirmGuardianConsent(String token, GuardianConsentDTO body, User currentUser);

    // Il genitore/tutore rifiuta: i dati della richiesta vengono cancellati subito
    void rejectGuardianConsent(String token);

    // Nuovo link per una richiesta ancora aperta, entro i limiti di invio
    void resendConfirmation(String email);

    // Job periodico: cancella le richieste scadute
    void purgeExpired();
}
