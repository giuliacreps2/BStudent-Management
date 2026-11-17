package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;

import java.util.List;

// Risultato di un test (blueprint §8). Test di livello: level (Principiante, Intermedio, Avanzato).
// Test di orientamento: le voci della Palestra consigliate, in ordine (vuoto = nessun consiglio).
// createdAt: data ISO dello svolgimento
public record StudentTestResultDTO(OrientationTestKind kind, CourseLevel level, List<PracticeItemDTO> recommended,
                                   String createdAt) {
}
