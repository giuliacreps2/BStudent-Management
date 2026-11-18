package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;

// "Continua la missione" nella home: una per materia (Blueprint Svolgimento §9).
// status dell'ultimo tentativo: active o saved (da riprendere), pending_review (in attesa della docente),
// failed (da ritentare o abbandonare)
public record CurrentMissionDTO(Long missionAttemptId, MissionAttemptStatus status, String subjectName,
                                String courseSlug, String missionSlug, String missionTitle) {
}
