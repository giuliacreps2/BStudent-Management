package giuliacrepaldi.BStudent_Management.payloads.gamification;

// Che cosa è successo dopo la valutazione di un tentativo della Palestra, per il messaggio allo studente
// (Blueprint Gamification §8b.5): vita del tris, sfida del giorno completata, invito alla pausa
public record PracticeRewardsDTO(boolean streakLifeAwarded, boolean dailyChallengeLifeAwarded,
                                 DailyChallengeDTO dailyChallenge, PracticeRunStateDTO run, LivesDTO lives) {
}
