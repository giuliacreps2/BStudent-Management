package giuliacrepaldi.BStudent_Management.services.identity.interfaces;

import giuliacrepaldi.BStudent_Management.entities.identity.Role;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoggedUserDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoginDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoginRespDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.RegisterAccountUserDTO;

import java.util.List;

/**
 * Registrazione, login, logout e utente corrente (blueprint §4).
 * Emette il JWT e lo consegna tramite cookie httpOnly (CookieUtils).
 * Lo stato iniziale dell'utente è pending_verification.
 */
public interface IAuthService {

    //REGISTRAZIONE

    String registerNewUserAccount(RegisterAccountUserDTO body);

    User verifyNewUserAccount(String token);


    //LOGIN

    LoginRespDTO login(LoginDTO body);

    //metodi per individuare l'utente e il ruolo che svolge, da definire se interessano all'applicazione
    LoggedUserDTO getCurrentUser(User user);

    LoggedUserDTO buildLoggedUserDTO(User user, List<Role> roles);


    //RESET PASSWORD

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);


    //LOGOUT
    //metodo inserito in Security Config da verificare e controllare
}
