package giuliacrepaldi.BStudent_Management.services.identity;

import giuliacrepaldi.BStudent_Management.entities.identity.Role;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.repositories.identity.RolesRepository;
import giuliacrepaldi.BStudent_Management.services.identity.interfaces.IRolesService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RolesService implements IRolesService {

    private final RolesRepository rolesRepository;

    public RolesService(RolesRepository rolesRepository) {
        this.rolesRepository = rolesRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findByUserId(Long userId) {
        return rolesRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Role findByCode(String code) {
        return rolesRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Ruolo " + code + " non trovato"));
    }
}
