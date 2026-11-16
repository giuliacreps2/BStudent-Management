package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.math.BigDecimal;

// Ricompense vere della pagina del corso (Blueprint Gamification §6.4): XP totali e, finché lo studente
// non ha completato il suo primo corso, l'Evoluzione I
public record CourseRewardsDTO(BigDecimal totalXp, boolean firstEvolution) {
}
