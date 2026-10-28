package giuliacrepaldi.BStudent_Management.payloads.identity;

// Dati letti dall'id_token OIDC di Google, già verificato da Spring Security.
// subject ("sub") è l'identificativo stabile; l'email può cambiare nel tempo
public record GoogleProfile(
        String subject,
        String email,
        boolean emailVerified,
        String givenName,
        String familyName) {
}
