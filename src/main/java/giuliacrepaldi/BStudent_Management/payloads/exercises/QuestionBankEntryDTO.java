package giuliacrepaldi.BStudent_Management.payloads.exercises;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Titolo editoriale, visibile solo all'Admin; il contenuto sta nelle versioni
public record QuestionBankEntryDTO(@NotBlank @Size(max = 200) String internalTitle) {
}
