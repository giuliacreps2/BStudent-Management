package giuliacrepaldi.BStudent_Management.services.identity.interfaces;

import giuliacrepaldi.BStudent_Management.entities.identity.Role;

import java.util.List;

/**
 * Catalogo dei ruoli e ruoli assegnati agli utenti (blueprint §4).
 */
public interface IRolesService {

    List<Role> findByUserId(Long userId);

    // NotFoundException se il codice non esiste
    Role findByCode(String code);
}
