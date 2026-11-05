package giuliacrepaldi.BStudent_Management.exceptions;

// 409: la richiesta è valida ma contrasta con lo stato attuale (es. contenuto già pubblicato,
// seconda missione in corso, stessa richiesta ripetuta con dati diversi)
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
