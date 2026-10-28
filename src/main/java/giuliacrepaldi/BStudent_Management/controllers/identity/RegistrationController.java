package giuliacrepaldi.BStudent_Management.controllers.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.identity.*;
import giuliacrepaldi.BStudent_Management.security.CookieUtils;
import giuliacrepaldi.BStudent_Management.security.TokenTools;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IAuthService;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Rotte pubbliche (/auth/**); i token dei link email viaggiano sempre nel body
@RestController
@RequestMapping("/auth")
public class RegistrationController {

    private final IRegistrationService registrationService;
    private final IAuthService authService;
    private final CookieUtils cookieUtils;
    private final TokenTools tokenTools;

    public RegistrationController(IRegistrationService registrationService, IAuthService authService,
                                  CookieUtils cookieUtils, TokenTools tokenTools) {
        this.registrationService = registrationService;
        this.authService = authService;
        this.cookieUtils = cookieUtils;
        this.tokenTools = tokenTools;
    }

    // 202 sempre: la risposta non rivela se l'email è già registrata
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void register(@RequestBody @Valid RegisterAccountUserDTO body) {
        registrationService.register(body);
    }

    // Dati per la pagina aperta dal link (conferma dello studente o consenso del genitore)
    @PostMapping("/registration/preview")
    public RegistrationPreviewDTO preview(@RequestBody @Valid TokenDTO body) {
        return registrationService.preview(body.token());
    }

    // Dai 14 anni: l'account viene creato e lo studente è subito autenticato
    @PostMapping("/registration/confirm")
    public ResponseEntity<LoggedUserDTO> confirmEmail(@RequestBody @Valid TokenDTO body) {
        User student = registrationService.confirmEmail(body.token());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE,
                        cookieUtils.createAccessTokenCookie(tokenTools.generateToken(student),
                                tokenTools.getTokenDuration()).toString())
                .body(authService.getCurrentUser(student.getUserId()));
    }

    // Sotto i 14 anni: currentUser è valorizzato se il genitore ha già un account ed è autenticato.
    // Nessun login automatico: l'account creato è dello studente, non di chi sta cliccando
    @PostMapping("/registration/guardian-consent")
    @ResponseStatus(HttpStatus.CREATED)
    public void confirmGuardianConsent(@AuthenticationPrincipal User currentUser,
                                       @RequestBody @Valid GuardianConsentDTO body) {
        registrationService.confirmGuardianConsent(body.token(), body, currentUser);
    }

    @PostMapping("/registration/guardian-consent/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectGuardianConsent(@RequestBody @Valid TokenDTO body) {
        registrationService.rejectGuardianConsent(body.token());
    }

    // 202 sempre: limiti di invio applicati in silenzio dal service
    @PostMapping("/registration/resend")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resendConfirmation(@RequestBody @Valid ResendConfirmationDTO body) {
        registrationService.resendConfirmation(body.email());
    }
}
