package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;

import java.util.List;

// Test come lo vede lo studente (blueprint §8): domande e opzioni a scelta singola, senza livelli né
// esercizi consigliati. Il risultato lo calcola il backend: il client manda solo le risposte
public record StudentTestDTO(Long testId, OrientationTestKind kind, String title, List<Question> questions) {

    public record Question(Long questionId, String text, List<Option> options) {
    }

    public record Option(Long optionId, String text) {
    }
}
