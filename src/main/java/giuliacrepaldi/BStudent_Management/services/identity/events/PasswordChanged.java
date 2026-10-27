package giuliacrepaldi.BStudent_Management.services.identity.events;

// Avviso di sicurezza al titolare dell'account dopo cambio o reset della password
public record PasswordChanged(String email) {
}
