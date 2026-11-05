package giuliacrepaldi.BStudent_Management.services.identity.interfaces;

import giuliacrepaldi.BStudent_Management.entities.identity.Role;

import java.util.List;

/**
 * Catalogo dei ruoli e ruoli assegnati agli utenti (blueprint §4).
 * Il primo ADMIN si crea a mano in user_roles (cercando il ruolo per code, non per id).
 * TODO: assignRole(userId, code) e revokeRole(userId, code) con endpoint /admin/users/{id}/roles
 * riservato all'ADMIN, per admin e docenti successivi; vietato togliere l'ultimo ADMIN (409).
 * In produzione un account admin separato da quello studente.
 */
public interface IRolesService {

    List<Role> findByUserId(Long userId);

    // NotFoundException se il codice non esiste
    Role findByCode(String code);
}
