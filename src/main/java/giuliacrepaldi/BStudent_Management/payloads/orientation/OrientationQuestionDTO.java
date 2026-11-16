package giuliacrepaldi.BStudent_Management.payloads.orientation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrientationQuestionDTO(@NotBlank @Size(max = 500) String text) {
}
