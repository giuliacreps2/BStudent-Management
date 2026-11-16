package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.math.BigDecimal;

// Livello dello studente in una skill di una materia (Blueprint Gamification §2).
// points = punti veri guadagnati; il livello può partire più in alto per il test di orientamento (§2.6).
// nextLevelPoints e pointsToNextLevel sono null al livello massimo
public record SkillLevelStateDTO(
        Long skillId,
        String code,
        String name,
        String icon,
        String color,
        BigDecimal points,
        int level,
        boolean maxLevel,
        int progressPercent,
        BigDecimal pointsToNextLevel) {
}
