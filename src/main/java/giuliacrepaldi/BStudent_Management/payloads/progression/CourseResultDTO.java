package giuliacrepaldi.BStudent_Management.payloads.progression;

import java.math.BigDecimal;
import java.util.List;

// Avanzamento del corso: missioni superate su quelle visibili e media dei migliori risultati.
// completedNow = il corso risulta completato con questa missione; newStages = evoluzioni (Evoluzione I al primo corso)
public record CourseResultDTO(Long courseId, String courseTitle, int passedMissions, int totalMissions,
                              BigDecimal percentage, boolean completedNow, List<Integer> newStages) {
}
