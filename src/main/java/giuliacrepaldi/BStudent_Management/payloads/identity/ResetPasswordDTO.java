package giuliacrepaldi.BStudent_Management.payloads.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Il token viaggia nel body e non nell'URL, così non finisce nei log di accesso
public record ResetPasswordDTO(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 72) String newPassword) {
}
