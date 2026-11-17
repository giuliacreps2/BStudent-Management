package giuliacrepaldi.BStudent_Management.payloads.student;

import java.math.BigDecimal;
import java.util.List;

// Missione sulla mappa del corso (blueprint §5.9). status calcolato dall'ordine: completed (superata),
// current (la prima non superata), locked (le successive). premiumLocked è un'informazione distinta: missione
// a pagamento e studente senza accesso. isFinal = prova finale. durationMinutes = somma dei tempi degli
// esercizi visibili. xp = "+N XP". Le posizioni sulla mappa le assegna il frontend secondo il tema
public record MissionNodeDTO(Long missionId, String slug, int order, String title, String tagline, String description,
                             String coverReference, boolean isFinal, String status, boolean premiumLocked,
                             int durationMinutes, BigDecimal xp, List<String> learningGoals) {
}
