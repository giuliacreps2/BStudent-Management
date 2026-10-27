package giuliacrepaldi.BStudent_Management.services.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.identity.Status;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.exceptions.UnauthorizedException;
import giuliacrepaldi.BStudent_Management.repositories.identity.UsersRepository;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IUsersService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsersService implements IUsersService {

    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User findForAuthentication(Long userId) {
        User user = usersRepository.findWithRolesByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException("Utente non trovato"));
        if (user.getStatus() == Status.suspended) {
            throw new UnauthorizedException("Account sospeso");
        }
        return user;
    }
}
