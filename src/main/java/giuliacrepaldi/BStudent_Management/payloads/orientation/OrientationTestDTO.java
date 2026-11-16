package giuliacrepaldi.BStudent_Management.payloads.orientation;

import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// La materia arriva dal path alla creazione; il tipo non cambia dopo la creazione
public record OrientationTestDTO(
        @NotNull OrientationTestKind kind,
        @NotBlank @Size(max = 200) String title) {
}
