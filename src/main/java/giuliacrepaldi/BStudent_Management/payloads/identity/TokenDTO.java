package giuliacrepaldi.BStudent_Management.payloads.identity;

import jakarta.validation.constraints.NotBlank;

// Token monouso ricevuto via email, inviato nel body e non nell'URL
public record TokenDTO(@NotBlank String token) {
}
