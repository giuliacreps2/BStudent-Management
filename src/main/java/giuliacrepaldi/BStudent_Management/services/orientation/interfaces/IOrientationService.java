package giuliacrepaldi.BStudent_Management.services.orientation.interfaces;

import giuliacrepaldi.BStudent_Management.entities.orientation.OrientationTest;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationOptionDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationQuestionDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationTestDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationTestDetailDTO;

import java.util.List;

/**
 * Test facoltativi per materia (blueprint §8): orientamento della Palestra e livello del corso.
 * Un test per materia e per tipo (exercises | course_level, v0.9). Domande e opzioni standard a scelta
 * singola; il risultato si calcola qui, il client manda solo le risposte. Nessun punto, vita o revisione.
 * Le risposte e il risultato di ogni svolgimento vengono salvati come fotografia (§8.3).
 * TODO legali e livelli di personalizzazione nel package-info di services.orientation.
 * <p>
 * Admin:
 * - il tipo si sceglie alla creazione e non cambia;
 * - course_level: ogni opzione ha un livello e nessun esercizio consigliato;
 * - exercises: nessun livello; zero o più voci della Palestra della stessa materia, con priorità &gt; 0;
 * - un'opzione già scelta in un risultato salvato non si cancella, e nemmeno la sua domanda o il suo test (§8.4);
 * - nessuno stato pubblicato: la lettura studente userà solo i test senza problemi (checkReadiness).
 * Le modifiche restituiscono il test intero aggiornato, che è la vista dell'editor.
 *
 * <h2>Parte studente: fatta in services.student.IStudentTestsService (traccia del contratto)</h2>
 * <pre>
 * OrientationTestViewDTO getTestForStudent(String subjectSlug, OrientationTestKind kind);
 *     // solo test senza problemi; domande e opzioni senza livelli né esercizi consigliati
 * OrientationResult submit(long studentId, String subjectSlug, OrientationTestKind kind,
 *                          Map&lt;Long, Long&gt; answers);
 *     // tutte le domande obbligatorie, una opzione per domanda, opzioni del test -> altrimenti BadRequest.
 *     // course_level: il livello più basso scelto almeno due volte; se nessuno si ripete, il più basso (§8.2).
 *     // exercises: al massimo 5 voci Palestra visibili della materia; prima quelle consigliate da più
 *     // risposte, poi priorità più bassa, poi ID (§8.1). Zero voci: risultato vuoto (TODO 3 del package-info).
 *     // Salva sempre answers e risultato in orientation_results.
 * Optional&lt;OrientationResult&gt; findLastResult(long studentId, String subjectSlug, OrientationTestKind kind);
 * </pre>
 * Entity da creare per la Fase 3: OrientationResult.
 */
public interface IOrientationService {

    //Admin: test

    OrientationTest saveNewTest(long subjectId, OrientationTestDTO body); // uno per materia e tipo

    List<OrientationTest> findBySubject(long subjectId);

    OrientationTestDetailDTO getDetail(long testId);

    OrientationTestDetailDTO updateTest(long testId, OrientationTestDTO body); // solo il titolo

    void deleteTest(long testId); // solo se nessuno studente l'ha ancora svolto

    //Admin: domande

    OrientationTestDetailDTO addQuestion(long testId, OrientationQuestionDTO body); // in coda

    OrientationTestDetailDTO updateQuestion(long questionId, OrientationQuestionDTO body);

    // Tutti e soli gli ID delle domande del test, nel nuovo ordine
    OrientationTestDetailDTO reorderQuestions(long testId, List<Long> questionIds);

    OrientationTestDetailDTO deleteQuestion(long questionId); // con le sue opzioni

    //Admin: opzioni

    OrientationTestDetailDTO addOption(long questionId, OrientationOptionDTO body); // in coda

    OrientationTestDetailDTO updateOption(long optionId, OrientationOptionDTO body);

    // Tutti e soli gli ID delle opzioni della domanda, nel nuovo ordine
    OrientationTestDetailDTO reorderOptions(long questionId, List<Long> optionIds);

    OrientationTestDetailDTO deleteOption(long optionId);

    // Proposta §8.2: almeno 3 domande, ognuna con almeno 2 opzioni; più le regole del tipo
    List<String> checkReadiness(long testId);
}
