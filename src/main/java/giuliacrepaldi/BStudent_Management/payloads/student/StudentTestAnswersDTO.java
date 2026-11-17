package giuliacrepaldi.BStudent_Management.payloads.student;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

// Risposte dello studente: per ogni domanda, l'opzione scelta. { "answers": { "12": 40, "13": 44 } }
public record StudentTestAnswersDTO(@NotEmpty Map<@NotNull Long, @NotNull Long> answers) {
}
