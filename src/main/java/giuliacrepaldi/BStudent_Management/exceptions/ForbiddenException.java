package giuliacrepaldi.BStudent_Management.exceptions;

// 403: utente autenticato ma senza diritto sulla risorsa (es. contenuto premium senza abbonamento,
// dato di un altro studente). I ruoli sulle rotte restano a SecurityConfig
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
