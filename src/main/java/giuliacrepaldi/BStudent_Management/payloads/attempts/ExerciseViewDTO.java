package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.AssessmentMethod;
import giuliacrepaldi.BStudent_Management.enums.exercises.KindQuestion;

import java.util.List;

// L'esercizio come lo vede lo studente mentre lo svolge: mai la soluzione (blueprint §6.1).
// options solo per la scelta multipla, nell'ordine salvato nel tentativo (ricaricare la pagina non le
// rimescola). assessmentMode = teacher: la consegna va alla docente e l'esito non è immediato
public record ExerciseViewDTO(String title, KindQuestion kind, String prompt, String sourceText,
                              AssessmentMethod assessmentMode, List<Option> options) {

    public record Option(Long answerId, String text) {
    }
}
