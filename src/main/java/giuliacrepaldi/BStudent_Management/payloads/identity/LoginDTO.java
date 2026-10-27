package giuliacrepaldi.BStudent_Management.payloads.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginDTO(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(max = 72) String password) {
}
