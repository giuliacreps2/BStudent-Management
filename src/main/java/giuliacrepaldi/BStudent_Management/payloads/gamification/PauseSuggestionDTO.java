package giuliacrepaldi.BStudent_Management.payloads.gamification;

// Invito alla pausa dopo alcuni errori di fila in Palestra (Blueprint Gamification §8.1-bis): un invito,
// non un blocco. La Risorsa è quella dell'argomento dell'ultimo esercizio sbagliato; null se non ce n'è una
public record PauseSuggestionDTO(String message, String topicName, String subjectSlug, String resourceSlug,
                                 String resourceTitle) {
}
