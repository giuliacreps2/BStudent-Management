package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;

// Tentativo della Palestra: stato, esercizio senza soluzione, risposta salvata ed esito quando c'è.
// status: active (in corso), saved (bozza), pending_review (in correzione), assessed (con esito),
// abandoned (lasciato senza salvare)
public record PracticeAttemptDTO(Long exerciseAttemptId, Long practiceExerciseId, ExerciseAttemptStatus status,
                                 ExerciseViewDTO exercise, AnswerDTO answer, ExerciseOutcomeDTO outcome) {
}
