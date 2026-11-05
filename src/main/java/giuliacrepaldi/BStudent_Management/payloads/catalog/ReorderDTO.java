package giuliacrepaldi.BStudent_Management.payloads.catalog;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Tutti e soli gli ID del contenitore, nel nuovo ordine (missioni, lezioni, pagine)
public record ReorderDTO(@NotEmpty List<@NotNull Long> ids) {
}
