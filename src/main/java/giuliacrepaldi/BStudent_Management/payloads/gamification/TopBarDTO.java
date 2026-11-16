package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.math.BigDecimal;

// Barra fissa dell'area studente (blueprint §10.5): vite, XP e stadio, sfida del giorno, Serie, cariche del
// cappello. La materia attiva resta nel frontend
public record TopBarDTO(LivesDTO lives, BigDecimal xp, int stage, DailyChallengeDTO dailyChallenge, StreakDTO streak,
                        HintChargesDTO hint) {
}
