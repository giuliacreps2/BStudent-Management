package giuliacrepaldi.BStudent_Management.services.billing.interfaces;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;

/**
 * Accesso ai contenuti premium: unico punto da interrogare, invece di leggere
 * abbonamenti e access grant sparsi nel codice.
 */
public interface IEntitlementService {

    // Abbonamento attivo che copre lo studente oppure access grant in corso (beta, promo, staff)
    boolean hasPremium(Long studentId);

    // Corso free: sempre; corso premium: solo con hasPremium
    boolean canAccess(Long studentId, Course course);

    // La missione può sovrascrivere il livello del corso (es. prime missioni free)
    boolean canAccess(Long studentId, Mission mission);

    // Ha avuto un accesso beta, anche scaduto: può usare lo sconto riservato
    boolean isBetaUser(Long studentId);
}
