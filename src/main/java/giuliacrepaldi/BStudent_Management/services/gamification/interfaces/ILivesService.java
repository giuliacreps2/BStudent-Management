package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

import giuliacrepaldi.BStudent_Management.enums.gamification.LifeAwardReason;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;

import java.time.LocalDate;

/**
 * Saldo vite e ricarica (blueprint principale §12.1; Blueprint Gamification §7.1, §8.1).
 * Base personale 5 (7 dopo l'Evoluzione I); una vita persa per tentativo di missione fallito.
 * Ricarica dieci ore dopo la prima vita persa, senza riavvio; alla scadenza saldo almeno pari alla base.
 * Vite extra: sfida giornaliera e tris della Palestra; possono superare la base e non riavviano la ricarica.
 * La Palestra non consuma vite. Vite e aiuti non si comprano.
 * Scadenze verificate dal server; saldi mai esposti come setter.
 */
public interface ILivesService {

    // Applica prima la ricarica scaduta; crea lo stato alla prima lettura con la base delle impostazioni
    LivesDTO getLives(long studentId);

    // Almeno una vita
    boolean canStartMission(long studentId);

    // +1, anche oltre la base; non tocca la ricarica. eventId già visto: nessun effetto.
    // Per la sfida giornaliera anche una sola vita per giorno (awardDay = giorno secondo l'ora italiana)
    LivesDTO awardLife(long studentId, LifeAwardReason reason, LocalDate awardDay, String eventId);

    // Evoluzione I: base a newBase, saldo almeno a newBase, ricarica invariata; ripetuta: nessun effetto
    LivesDTO raiseBaseLives(long studentId, int newBase);

    // Evento in arrivo dal Blocco 5 (svolgimento): -1 una sola volta per tentativo fallito
    // (mission_life_losses); avvia la ricarica solo se non è già attiva
    LivesDTO onMissionAttemptFailed(long studentId, long missionAttemptId);
}
