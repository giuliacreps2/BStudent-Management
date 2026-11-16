package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.gamification.DailyChallengeDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeRewardsDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.PracticeRunStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StreakDTO;

/**
 * Sfida giornaliera, Serie, tris per una vita e pausa (Blueprint Gamification §7, §8.1, §8.1-bis).
 * Tutto si calcola dai tentativi valutati della Palestra e delle missioni; il giorno segue l'ora italiana.
 * Sfida: 3 voci della Palestra superate oggi (almeno 60%), ognuna una volta: 1 vita, una volta al giorno.
 * Tris: 3 voci superate di fila: 1 vita; un errore azzera; la stessa voce conta una volta al giorno.
 * Serie: un giorno conta con almeno un esercizio superato (Palestra o missione); un giorno di pausa non la
 * azzera, due di fila sì; si mostra anche il record. Nessun premio e nessun avviso di "perdita".
 * Pausa: dopo 3 errori di fila in Palestra, invito a rileggere la teoria; non blocca e non toglie vite.
 * I calcoli stanno in utilities.GameRules (coperti dai test).
 *
 * <p>TODO (programma, §3b.4): gli esercizi "più avanti nel programma" non contano e non azzerano. Oggi
 * contano tutti, perché la classe dello studente non esiste ancora (Piano MVP, P.3).
 */
public interface IDailyGoalsService {

    DailyChallengeDTO getDailyChallenge(long studentId);

    StreakDTO getStreak(long studentId);

    PracticeRunStateDTO getPracticeRunState(long studentId);

    // Evento in arrivo dal Blocco 5 (svolgimento): dopo la valutazione di un tentativo della Palestra, subito
    // o all'esito della docente. Assegna le vite del tris e della sfida, una sola volta per evento
    PracticeRewardsDTO onPracticeAttemptEvaluated(long studentId, long exerciseAttemptId);
}
