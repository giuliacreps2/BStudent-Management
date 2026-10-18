package giuliacrepaldi.BStudent_Management.exceptions;

import java.util.List;

public class ValidationException extends RuntimeException {

    private List<String> errors;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, List<String> errors) {
        super("Validation Failed");
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
