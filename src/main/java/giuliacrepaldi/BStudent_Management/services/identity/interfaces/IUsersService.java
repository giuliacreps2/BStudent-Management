package giuliacrepaldi.BStudent_Management.services.identity.interfaces;

import giuliacrepaldi.BStudent_Management.entities.identity.User;

/**
 * Lettura degli utenti (blueprint §4).
 * Gli utenti cancellati sono esclusi da @SQLRestriction su User.
 */
public interface IUsersService {

    // NotFoundException se l'utente non esiste
    User findById(Long userId);

    // Per TokenFilter: utente con ruoli già caricati; UnauthorizedException se inesistente o sospeso
    User findForAuthentication(Long userId);
}
