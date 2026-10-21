package giuliacrepaldi.BStudent_Management.services.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.Role;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoggedUserDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoginDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.LoginRespDTO;
import giuliacrepaldi.BStudent_Management.payloads.identity.RegisterAccountUserDTO;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IAuthService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService implements IAuthService {
    @Override
    public String registerNewUserAccount(RegisterAccountUserDTO body) {
        return "";
    }

    @Override
    public User verifyNewUserAccount(String token) {
        return null;
    }

    @Override
    public LoginRespDTO login(LoginDTO body) {
        return null;
    }

    @Override
    public LoggedUserDTO getCurrentUser(User user) {
        return null;
    }

    @Override
    public LoggedUserDTO buildLoggedUserDTO(User user, List<Role> roles) {
        return null;
    }

    @Override
    public void requestPasswordReset(String email) {

    }

    @Override
    public void resetPassword(String token, String newPassword) {

    }
}
