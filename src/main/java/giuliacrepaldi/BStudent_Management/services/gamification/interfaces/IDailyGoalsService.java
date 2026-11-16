package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

/**
 * Sfida giornaliera, Serie, tris per una vita e pausa (Blueprint Gamification §7, §8.1, §8.1-bis).
 * Tutto si calcola dai tentativi valutati della Palestra e delle missioni; il giorno segue l'ora italiana.
 * Sfida: 3 voci della Palestra superate oggi (almeno 60%), ognuna una volta: 1 vita, una volta al giorno.
 * Tris: 3 voci superate di fila: 1 vita; un errore azzera; la stessa voce conta una volta al giorno.
 * Serie: un giorno conta con almeno un esercizio superato (Palestra o missione); un giorno di pausa non la
 * azzera, due di fila sì; si mostra anche il record. Nessun premio e nessun avviso di "perdita".
 * Pausa: dopo 3 errori di fila in Palestra, invito a rileggere la teoria; non blocca e non toglie vite.
 *
 * <h2>TODO — metodi da implementare (Fase D)</h2>
 * <pre>
 * // Calcoli puri, coperti dai test (utilities): ricevono dati già letti
 * static StreakDTO streakOf(Collection&lt;LocalDate&gt; studyDays, LocalDate today);
 *     // lun, mar, (mer pausa), gio, ven -&gt; 4; due giorni vuoti di fila -&gt; riparte da 1; record conservato.
 *     // Oggi ancora senza studio non interrompe: la Serie si valuta fino a ieri
 * static int passedInARow(List&lt;PracticeOutcome&gt; outcomesInOrder);
 *     // conteggio verso il tris dopo l'ultimo premio o l'ultimo errore
 * static int failedInARow(List&lt;PracticeOutcome&gt; outcomesInOrder);
 *
 * DailyChallengeDTO getDailyChallenge(long studentId);
 *     // superati oggi su 3, completata sì/no, vita già ricevuta sì/no
 * StreakDTO getStreak(long studentId);
 *     // giorni attuali e record
 * PracticeRunStateDTO getPracticeRunState(long studentId);
 *     // superati di fila verso la prossima vita (0-2) e se proporre la pausa, con l'argomento e la
 *     // Risorsa dell'ultimo esercizio sbagliato
 *
 * // Evento in arrivo dal Blocco 5: contratto, resta TODO finché lo svolgimento non esiste
 * PracticeRewardsDTO onPracticeAttemptEvaluated(long studentId, long exerciseAttemptId);
 *     // dopo la valutazione di un tentativo della Palestra (subito, o all'esito della docente):
 *     // assegna le vite del tris e della sfida con ILivesService.awardLife, una sola volta per evento
 *     // (life_awards con chiave unica); restituisce che cosa è successo, per il messaggio allo studente
 * </pre>
 * DTO da creare: DailyChallengeDTO, StreakDTO, PracticeRunStateDTO, PracticeRewardsDTO.
 * Controller: GET /student/daily-challenge, GET /student/streak (anche dentro la barra fissa).
 */
public interface IDailyGoalsService {
}
