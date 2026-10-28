package giuliacrepaldi.BStudent_Management.payloads.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Email dello studente indicata nella registrazione ancora in attesa
public record ResendConfirmationDTO(@NotBlank @Email @Size(max = 320) String email) {
}
