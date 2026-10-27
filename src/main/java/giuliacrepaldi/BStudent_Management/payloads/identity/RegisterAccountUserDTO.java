package giuliacrepaldi.BStudent_Management.payloads.identity;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterAccountUserDTO(
        @NotBlank @Email @Size(max = 320) String email,
        // BCrypt considera al massimo 72 byte
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Size(max = 80) String username,
        Long schoolId,
        // Obbligatoria sotto i 14 anni: lo verifica il service, che calcola l'età
        @Email @Size(max = 320) String guardianEmail,
        @NotBlank @Size(max = 40) String policyVersion) {
}
