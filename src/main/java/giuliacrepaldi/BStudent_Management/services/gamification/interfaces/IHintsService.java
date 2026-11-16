package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

/**
 * Aiuto del cappello (blueprint principale §11.3; Blueprint Gamification §6.2).
 * Solo nelle missioni, con il cappello indossato. L'aiuto dice l'argomento dell'esercizio e mostra
 * l'"In breve" della Risorsa collegata a quell'argomento, anche se la Risorsa è premium.
 * Un nuovo aiuto consuma una carica; riaprirlo nello stesso tentativo è gratuito.
 * 5 cariche; ricarica completa 12 ore dopo l'esaurimento dell'ultima.
 *
 * <h2>TODO — metodi da implementare (Fase D)</h2>
 * <pre>
 * HintChargesDTO getCharges(long studentId);
 *     // applica prima la ricarica scaduta; cariche rimaste e ora della ricarica; "non posseduto" se manca
 * // Calcoli puri, coperti dai test:
 * static ChargeState afterUse(int remaining, int rechargeHours, OffsetDateTime now);
 *     // 5 -&gt; 4 senza timer; 1 -&gt; 0 con ricarica tra 12 ore
 * static ChargeState afterRecharge(ChargeState state, int capacity, OffsetDateTime now);
 * HintDTO useHint(long studentId, long missionAttemptId, long missionExerciseId);
 *     // tentativo aperto e dello studente; esercizio della missione del tentativo; cappello indossato.
 *     // Esercizio senza argomento: BadRequestException "Per questo esercizio il cappello non ha
 *     // suggerimenti", senza consumare cariche. Già aperto in questo tentativo (hint_uses, con
 *     // hint_id = esercizio della missione): stesso aiuto, nessuna carica. Senza cariche:
 *     // ConflictException con l'ora della ricarica.
 *     // Risposta: argomento, "In breve" (testo e mini esempio) se esiste una Risorsa pubblicata
 *     // dell'argomento, cariche rimaste. Il collegamento alla Risorsa intera compare solo a fine tentativo
 * List&lt;HintReviewLinkDTO&gt; getReviewLinks(long studentId, long missionAttemptId);
 *     // a tentativo chiuso: per ogni aiuto usato, argomento e Risorsa da ripassare ("Ripassa in Palestra")
 * </pre>
 * Dipendenza: la tabella delle Risorse nasce nel branch dei corsi. Finché non c'è, l'aiuto mostra solo
 * l'argomento.
 * TODO dopo la beta: catalogo di suggerimenti a partire dagli errori degli studenti; aiuti premium (§6.2).
 * DTO da creare: HintDTO, HintChargesDTO, HintReviewLinkDTO.
 * Controller: POST /student/mission-attempts/{id}/exercises/{missionExerciseId}/hint.
 */
public interface IHintsService {
}
