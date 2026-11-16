package giuliacrepaldi.BStudent_Management.payloads.orientation;

import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationTest;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;

// Riga dell'elenco dei test della materia
public record OrientationTestResponseDTO(Long orientationTestId, Long subjectId, OrientationTestKind kind, String title) {

    public static OrientationTestResponseDTO from(OrientationTest test) {
        return new OrientationTestResponseDTO(test.getOrientationTestId(), test.getSubject().getSubjectId(),
                test.getKind(), test.getTitle());
    }
}
