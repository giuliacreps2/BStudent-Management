package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.math.BigDecimal;

// XP dello studente e cammino verso la prossima evoluzione (Blueprint Gamification §4).
// nextStage è null dopo l'ultimo stadio. Per uno stadio con soglie: nextMinXp, xpMissing e
// nextMinAverageLevel; per l'Evoluzione I (primo corso completato) solo nextRequiresFirstCourse = true
public record EvolutionProgressDTO(
        int currentStage,
        BigDecimal totalXp,
        BigDecimal bestAverageLevel,
        Integer nextStage,
        boolean nextRequiresFirstCourse,
        BigDecimal nextMinXp,
        BigDecimal xpMissing,
        BigDecimal nextMinAverageLevel,
        boolean averageLevelReached) {
}
