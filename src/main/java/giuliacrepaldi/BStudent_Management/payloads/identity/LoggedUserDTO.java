package giuliacrepaldi.BStudent_Management.payloads.identity;

import java.util.Set;

// studentId e username valorizzati solo se l'utente ha un profilo studente
public record LoggedUserDTO(
        Long userId,
        String email,
        String firstName,
        String lastName,
        Set<String> roles,
        boolean emailVerified,
        // false per gli account solo Google: il frontend propone "imposta una password"
        boolean hasLocalPassword,
        // Identità Google collegata: la pagina Sicurezza del frontend mostra "Collegato"
        boolean googleLinked,
        Long studentId,
        String username) {
}
