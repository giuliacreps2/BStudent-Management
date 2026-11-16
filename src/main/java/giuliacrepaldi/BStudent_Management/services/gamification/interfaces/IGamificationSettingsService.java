package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

/**
 * Impostazioni di gioco, lato Admin (riga unica gamification_settings).
 * Numeri che si possono cambiare dopo aver osservato la beta (Blueprint Gamification §9), senza toccare il codice.
 *
 * <h2>TODO — metodi da implementare (Fase D)</h2>
 * <pre>
 * GamificationSetting get();
 * GamificationSetting update(GamificationSettingsDTO body);
 *     // vite di base (5), ore della ricarica (10), esercizi della sfida giornaliera (3),
 *     // esercizi di fila per una vita (3), errori di fila per la pausa (3). Tutti interi positivi.
 *     // Le modifiche valgono da ora: vite e premi già assegnati non cambiano
 * </pre>
 * Nell'interfaccia Admin: "Impostazioni di gioco", con etichette in italiano e conferma visibile al salvataggio.
 * DTO da creare: GamificationSettingsDTO. Controller: /admin/academy/game-settings.
 */
public interface IGamificationSettingsService {
}
