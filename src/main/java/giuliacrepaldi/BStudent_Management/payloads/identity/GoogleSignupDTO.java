package giuliacrepaldi.BStudent_Management.payloads.identity;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

// Secondo passaggio della registrazione con Google: email e nome arrivano da Google,
// la data di nascita no (lo scope birthday è sensibile e spesso assente)
public record GoogleSignupDTO(
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Size(max = 80) String username,
        Long schoolId,
        // Obbligatoria sotto i 14 anni: lo verifica il service, che calcola l'età
        @Email @Size(max = 320) String guardianEmail,
        @NotBlank @Size(max = 40) String policyVersion) {
}
