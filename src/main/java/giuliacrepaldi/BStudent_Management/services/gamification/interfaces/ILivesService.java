package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

/**
 * Saldo vite e ricarica (blueprint principale §12.1; Blueprint Gamification §7.1, §8.1).
 * Base personale 5 (7 dopo l'Evoluzione I); una vita persa per tentativo di missione fallito.
 * Ricarica dieci ore dopo la prima vita persa, senza riavvio; alla scadenza saldo almeno pari alla base.
 * Vite extra: sfida giornaliera e tris della Palestra; possono superare la base e non riavviano la ricarica.
 * La Palestra non consuma vite. Vite e aiuti non si comprano.
 * Scadenze verificate dal server; saldi mai esposti come setter.
 *
 * <h2>TODO — metodi da implementare (Fase D)</h2>
 * <pre>
 * // Calcoli puri, coperti dai test:
 * static int afterRecharge(int lives, int baseLives);     // base 5, saldo 3 -&gt; 5; base 5, saldo 7 -&gt; 7
 * static int afterBaseRaise(int lives, int newBase);      // nuova base 7: saldo 3 -&gt; 7; saldo 9 -&gt; 9
 *
 * LivesDTO getLives(long studentId);
 *     // applica prima la ricarica scaduta (rechargeAt nel passato: saldo almeno alla base, timer spento);
 *     // crea lo stato alla prima lettura con la base delle impostazioni
 * boolean canStartMission(long studentId);
 *     // almeno una vita
 * LivesDTO awardLife(long studentId, LifeAwardReason reason, String eventId);
 *     // +1, anche oltre la base; non tocca rechargeAt; eventId già visto: nessun effetto (life_awards)
 * LivesDTO raiseBaseLives(long studentId, int newBase);
 *     // Evoluzione I: base a 7, saldo almeno a 7, timer invariato; ripetuta: nessun effetto
 *
 * // Evento in arrivo dal Blocco 5: contratto
 * LivesDTO onMissionAttemptFailed(long studentId, long missionAttemptId);
 *     // -1 una sola volta per tentativo (mission_life_losses); avvia la ricarica solo se non è già attiva
 * </pre>
 * DTO da creare: LivesDTO (saldo, base, ora della ricarica). Enum: LifeAwardReason (daily_challenge, practice_streak).
 * Controller: GET /student/lives (anche dentro la barra fissa).
 */
public interface ILivesService {
}
