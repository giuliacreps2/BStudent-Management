package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;

import java.math.BigDecimal;

// Esercizio della missione mentre lo studente lo svolge: mai la soluzione (blueprint §6.1).
// remainingMs = tempo rimasto adesso, verificato dal server; timerRunning = il tempo scorre (false se la
// missione è in pausa o l'esercizio è consegnato). Il frontend fa il conto alla rovescia partendo da qui e
// sincronizza la risposta ogni pochi secondi
public record MissionExercisePlayDTO(Long exerciseAttemptId, Long missionExerciseId, ExerciseAttemptStatus status,
                                     ExerciseViewDTO exercise, AnswerDTO answer, BigDecimal maxPoints,
                                     long allowedMs, long remainingMs, boolean timerRunning) {
}
