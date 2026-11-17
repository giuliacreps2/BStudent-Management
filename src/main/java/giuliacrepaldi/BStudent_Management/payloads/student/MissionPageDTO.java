package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.payloads.gamification.HintChargesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.MissionRewardsDTO;

import java.math.BigDecimal;
import java.util.List;

// Pagina della missione (blueprint §5.9). passingThreshold = soglia di superamento (60%), mostrata al posto
// del "massimo errori". canStart = missione attuale o già completata, accessibile e con almeno una vita.
// lives e hint sono i contatori in alto (vite e cariche del cappello). Classifica: fuori dall'MVP
public record MissionPageDTO(MissionNodeDTO mission, String courseSlug, String courseTitle, BigDecimal passingThreshold,
                             MissionRewardsDTO rewards, List<MissionStepDTO> steps, MissionBriefingDTO briefing,
                             LivesDTO lives, HintChargesDTO hint, boolean canStart, boolean tutorialSeen) {
}
