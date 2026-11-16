package giuliacrepaldi.BStudent_Management.payloads.gamification;

// Sfida giornaliera "supera N oggi" (Blueprint Gamification §7.1): voci diverse della Palestra superate oggi.
// lifeAwarded = la vita di oggi è già stata ricevuta
public record DailyChallengeDTO(int passedToday, int target, boolean completed, boolean lifeAwarded) {
}
