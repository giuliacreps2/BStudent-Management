package giuliacrepaldi.BStudent_Management.payloads.attempts;

import jakarta.validation.constraints.Size;

// Risposta dello studente: l'opzione scelta (scelta multipla) oppure il testo (completamento, traduzioni).
// Vuota = nessuna risposta ancora
public record AnswerDTO(Long selectedAnswerId, @Size(max = 20000) String text) {

    public static AnswerDTO empty() {
        return new AnswerDTO(null, null);
    }

    public boolean isEmpty() {
        return selectedAnswerId == null && (text == null || text.isBlank());
    }
}
