package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

import giuliacrepaldi.BStudent_Management.entities.gamification.GamificationSetting;
import giuliacrepaldi.BStudent_Management.payloads.gamification.GamificationSettingsDTO;

/**
 * Impostazioni di gioco, lato Admin (riga unica gamification_settings).
 * Numeri che si possono cambiare dopo aver osservato la beta (Blueprint Gamification §9), senza toccare il codice:
 * vite di base, ore della ricarica, esercizi della sfida giornaliera, esercizi di fila per una vita,
 * errori di fila per la pausa. Le modifiche valgono da ora: vite e premi già assegnati non cambiano.
 * Nell'interfaccia Admin: "Impostazioni di gioco", con conferma visibile al salvataggio.
 */
public interface IGamificationSettingsService {

    GamificationSetting get();

    GamificationSetting update(GamificationSettingsDTO body);
}
