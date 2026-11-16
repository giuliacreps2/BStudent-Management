package giuliacrepaldi.BStudent_Management.payloads.gamification;

// A che punto è lo studente verso la prossima vita: voci superate di fila su quelle richieste (§8.1).
// pause è valorizzato quando conviene proporre la pausa
public record PracticeRunStateDTO(int passedInARow, int streakForLife, PauseSuggestionDTO pause) {
}
