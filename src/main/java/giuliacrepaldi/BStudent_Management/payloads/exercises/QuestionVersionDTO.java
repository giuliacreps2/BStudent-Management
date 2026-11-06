package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.exercises.KindQuestion;
import giuliacrepaldi.BStudent_Management.enums.exercises.RubricCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// Contenuto completo di una versione: domanda, risposte nell'ordine della lista, rubriche.
// Le regole per tipo (quante risposte, quante corrette, rubriche solo sulle traduzioni di versione)
// le verifica il service
public record QuestionVersionDTO(
        @NotNull KindQuestion kind,
        @NotBlank String prompt,
        // Obbligatorio per le traduzioni, assente per scelta multipla e completamento
        String sourceText,
        // Obbligatoria per le traduzioni
        String generalExplanation,
        // Obbligatoria solo per la traduzione di frase; per gli altri tipi è fissa e si può omettere
        AssessmentMethod assessmentMode,
        @Size(max = 20) List<@Valid @NotNull AnswerDTO> answers,
        @Size(max = 20) List<@Valid @NotNull RubricCriterionDTO> rubricCriteria) {

    // correct conta solo per la scelta multipla: negli altri tipi ogni risposta è una soluzione accettata
    public record AnswerDTO(@NotBlank String text, boolean correct, String feedback) {
    }

    public record RubricCriterionDTO(@NotNull RubricCategory category, @NotBlank String description) {
    }
}
