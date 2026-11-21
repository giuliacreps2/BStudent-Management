package giuliacrepaldi.BStudent_Management.payloads.notebook;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Colore della copertina: chiave della tavolozza del frontend (es. "blu", "verde-salvia"); null = predefinito
public record CoverColorDTO(@Size(max = 30) @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
        message = "Colore non valido") String color) {
}
