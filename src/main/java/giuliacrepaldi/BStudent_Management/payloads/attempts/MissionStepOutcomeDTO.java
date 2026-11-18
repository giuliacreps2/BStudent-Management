package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.progression.MissionPassedDTO;

import java.util.List;

// Che cosa è successo dopo un passo (Blueprint Svolgimento §3).
// result: lesson_read (lezione letta, si va avanti), passed (esercizio superato, si va avanti),
// failed (sotto la soglia: tentativo fallito, una vita persa), expired (tempo finito: tentativo fallito, una vita
// persa), pending_review (consegnato alla docente), withdrawn (esercizio ritirato dall'autrice: saltato, nessuna
// vita persa), mission_passed (missione superata: vedi missionPassed).
// outcome = esito dell'esercizio, se c'è; lifeLost = una vita persa adesso; newStages = evoluzioni raggiunte
// adesso (punti skill o missione superata); state = la missione dopo il passo
public record MissionStepOutcomeDTO(String result, ExerciseOutcomeDTO outcome, boolean lifeLost, LivesDTO lives,
                                    MissionPassedDTO missionPassed, List<Integer> newStages, MissionPlayDTO state) {
}
