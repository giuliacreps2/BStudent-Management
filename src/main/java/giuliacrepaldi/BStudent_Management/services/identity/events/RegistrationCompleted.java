package giuliacrepaldi.BStudent_Management.services.identity.events;

// Pubblicato quando l'account dello studente è stato creato; guardianEmail è null dai 14 anni in su
public record RegistrationCompleted(
        String studentEmail,
        String studentName,
        String guardianEmail) {
}
