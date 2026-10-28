package giuliacrepaldi.BStudent_Management.payloads.identity;

import giuliacrepaldi.BStudent_Management.enums.identity.Relationship;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GuardianConsentDTO(
        // Token ricevuto via email: nel body e non nell'URL
        @NotBlank String token,
        @NotNull Relationship relationship,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        // Solo se il genitore/tutore non ha ancora un account; altrimenti deve essere autenticato
        @Size(min = 8, max = 72) String password,
        @AssertTrue boolean consentGiven,
        @NotBlank @Size(max = 40) String policyVersion) {
}
