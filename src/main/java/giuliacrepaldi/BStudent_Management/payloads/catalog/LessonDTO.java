package giuliacrepaldi.BStudent_Management.payloads.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// La missione arriva dal path alla creazione; ordine e stato cambiano solo con reorder e publish/withdraw
public record LessonDTO(@NotBlank @Size(max = 200) String title) {
}
