package giuliacrepaldi.BStudent_Management.payloads.progression;

import java.math.BigDecimal;
import java.util.List;

// Missione superata: risultato (media degli esercizi, pesata sui punti), se è il nuovo record, XP ottenuti
// adesso (solo al primo superamento), evoluzioni raggiunte e corso
public record MissionPassedDTO(BigDecimal percentage, boolean newRecord, BigDecimal xpEarned, List<Integer> newStages,
                               CourseResultDTO course) {
}
