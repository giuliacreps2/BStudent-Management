package giuliacrepaldi.BStudent_Management.payloads.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(
        @NotBlank @Size(max = 72) String currentPassword,
        // BCrypt considera al massimo 72 byte
        @NotBlank @Size(min = 8, max = 72) String newPassword) {
}
