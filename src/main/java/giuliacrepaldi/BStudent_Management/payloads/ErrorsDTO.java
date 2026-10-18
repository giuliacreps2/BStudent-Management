package giuliacrepaldi.BStudent_Management.payloads;

import java.time.OffsetDateTime;

public record ErrorsDTO(String message, OffsetDateTime now) {
}
