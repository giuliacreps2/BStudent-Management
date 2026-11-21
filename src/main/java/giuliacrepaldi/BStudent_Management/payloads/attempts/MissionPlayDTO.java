package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.payloads.gamification.HintChargesDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.LivesDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.MissionStepDTO;
import giuliacrepaldi.BStudent_Management.enums.attempts.FailureReason;
import giuliacrepaldi.BStudent_Management.enums.attempts.MissionAttemptStatus;

import java.util.List;

// Svolgimento della missione (blueprint §9; Blueprint Svolgimento).
// status: active (in corso), saved (in pausa: "Riprendi"), pending_review (in attesa della docente),
// failed (tentativo fallito: failureReason dice perché; si ritenta con start), passed (superata).
// steps: le tappe con stato done | current | todo. currentStep = numero della tappa attuale, null a missione finita.
// Al passo attuale c'è la lezione (lesson) oppure l'esercizio (exercise), mai tutti e due.
// lives e hint sono i contatori in alto (vite e cariche del cappello).
// teacherReview = esito dell'ultimo esercizio corretto dalla docente in questo tentativo (percentuale e commento):
// lo studente lo vede riaprendo la missione (Blueprint Svolgimento §4.3); null se non c'è
public record MissionPlayDTO(Long missionRunId, Long missionAttemptId, int attemptNumber, String courseSlug,
                             String missionSlug, String missionTitle, String subjectName, MissionAttemptStatus status,
                             FailureReason failureReason, List<MissionStepDTO> steps, Integer currentStep,
                             LessonPlayDTO lesson, MissionExercisePlayDTO exercise, LivesDTO lives,
                             HintChargesDTO hint, ExerciseOutcomeDTO teacherReview) {
}
