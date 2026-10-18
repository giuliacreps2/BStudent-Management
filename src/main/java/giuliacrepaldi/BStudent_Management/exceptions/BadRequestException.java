package giuliacrepaldi.BStudent_Management.exceptions;

import java.util.List;

public class BadRequestException extends RuntimeException {

    private List<String> errors;

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
}
