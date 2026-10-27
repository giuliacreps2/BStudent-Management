package giuliacrepaldi.BStudent_Management.controllers.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.identity.*;
import giuliacrepaldi.BStudent_Management.security.CookieUtils;
import giuliacrepaldi.BStudent_Management.security.TokenTools;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final IAuthService authService;
    private final CookieUtils cookieUtils;
    private final TokenTools tokenTools;

    public AuthController(IAuthService authService, CookieUtils cookieUtils, TokenTools tokenTools) {
        this.authService = authService;
        this.cookieUtils = cookieUtils;
        this.tokenTools = tokenTools;
    }

    //LOGIN: il JWT va nel cookie httpOnly, nel body solo i dati dell'utente
    @PostMapping("/login")
    public ResponseEntity<LoggedUserDTO> login(@RequestBody @Valid LoginDTO body) {
        AuthResult result = authService.login(body);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookieUtils.createAccessTokenCookie(result.accessToken(), tokenTools.getTokenDuration()).toString())
                .body(result.user());
    }

    //LOGOUT: stateless, basta cancellare il cookie
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieUtils.clearAccessTokenCookie().toString())
                .build();
    }

    @GetMapping("/me")
    public LoggedUserDTO me(@AuthenticationPrincipal User currentUser) {
        return authService.getCurrentUser(currentUser.getUserId());
    }

    //PASSWORD
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal User currentUser,
                               @RequestBody @Valid ChangePasswordDTO body) {
        authService.changePassword(currentUser.getUserId(), body.currentPassword(), body.newPassword());
    }

    // 202 sempre: la risposta non rivela se l'email è registrata
    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPassword(@RequestBody @Valid ForgotPasswordDTO body) {
        authService.requestPasswordReset(body.email());
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody @Valid ResetPasswordDTO body) {
        authService.resetPassword(body.token(), body.newPassword());
    }

    //VERIFICA EMAIL
    @PostMapping("/me/email-verification")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestEmailVerification(@AuthenticationPrincipal User currentUser) {
        authService.requestEmailVerification(currentUser.getUserId());
    }

    @PostMapping("/email/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestBody @Valid TokenDTO body) {
        authService.verifyEmail(body.token());
    }
}
