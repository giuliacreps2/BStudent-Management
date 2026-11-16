package giuliacrepaldi.BStudent_Management.payloads.exercises;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Il contenuto sta nelle versioni
public record QuestionBankEntryDTO(
        // Titolo editoriale, visibile solo all'Admin
        @NotBlank @Size(max = 200) String internalTitle,
        // Titolo per lo studente: facoltativo in bozza, obbligatorio per pubblicare
        @Size(max = 200) String title,
        // Argomento facoltativo (collegamento alla teoria)
        Long topicId) {
}
