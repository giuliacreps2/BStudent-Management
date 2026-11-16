package giuliacrepaldi.BStudent_Management.payloads.gamification;

// Lucchetto della difficoltà di una voce della Palestra (Blueprint Gamification §3), distinto dal
// lucchetto premium. message = "Si sblocca al livello N di [skill]"; null se la voce è aperta
public record PracticeLockDTO(Long practiceExerciseId, int difficulty, boolean locked, Integer unlockLevel,
                              String message) {
}
