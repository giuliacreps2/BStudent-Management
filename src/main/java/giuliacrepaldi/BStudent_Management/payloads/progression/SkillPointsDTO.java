package giuliacrepaldi.BStudent_Management.payloads.progression;

import java.math.BigDecimal;
import java.util.List;

// Punti skill accreditati adesso (solo la parte in più rispetto al proprio risultato migliore) ed evoluzioni
// raggiunte grazie a loro
public record SkillPointsDTO(BigDecimal earned, List<Integer> newStages) {
}
