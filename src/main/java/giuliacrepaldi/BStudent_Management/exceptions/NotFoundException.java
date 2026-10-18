package giuliacrepaldi.BStudent_Management.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(long id) {
        super("This id " + id + " not found");
    }
}
