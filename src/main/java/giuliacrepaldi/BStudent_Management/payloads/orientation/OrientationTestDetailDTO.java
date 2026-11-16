package giuliacrepaldi.BStudent_Management.payloads.orientation;

import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;

import java.util.List;

// Il test intero per l'editor dell'Admin: ogni modifica restituisce questa vista aggiornata.
// problems vuota = il test è pronto per gli studenti
public record OrientationTestDetailDTO(
        Long orientationTestId,
        Long subjectId,
        OrientationTestKind kind,
        String title,
        List<Question> questions,
        List<String> problems) {

    public record Question(Long questionId, String text, int sortOrder, List<Option> options) {
    }

    public record Option(Long optionId, String text, int sortOrder, CourseLevel level,
                         List<Recommendation> recommendations) {
    }

    // title = titolo per lo studente dell'esercizio consigliato
    public record Recommendation(Long practiceExerciseId, String title, int priority) {
    }
}
