package giuliacrepaldi.BStudent_Management.services.orientation.interfaces;

/**
 * Test facoltativi per materia (blueprint §8): orientamento della Palestra e livello del corso.
 * Un test per materia e per tipo (exercises | course_level, v0.9). Domande e opzioni standard a scelta
 * singola; il risultato si calcola qui, il client manda solo le risposte. Nessun punto, vita o revisione.
 * Le risposte e il risultato di ogni svolgimento vengono salvati come fotografia (§8.3).
 * TODO legali e livelli di personalizzazione nel package-info di services.orientation.
 *
 * <h2>TODO — metodi da implementare</h2>
 * <b>Admin (Fase 1)</b>
 * <pre>
 * OrientationTest saveNewTest(long subjectId, OrientationTestDTO body);
 *     // tipo e titolo; uno per materia e tipo -> ConflictException
 * OrientationTest findById(long testId);
 * List&lt;OrientationTest&gt; findBySubject(long subjectId);
 * OrientationTest findByIdAndUpdate(long testId, OrientationTestDTO body);   // il tipo non cambia
 * OrientationQuestion addQuestion(long testId, OrientationQuestionDTO body);  // in coda
 * OrientationQuestion updateQuestion(long questionId, OrientationQuestionDTO body);
 * List&lt;OrientationQuestion&gt; reorderQuestions(long testId, List&lt;Long&gt; questionIds);
 * void deleteQuestion(long questionId);
 * OrientationOption addOption(long questionId, OrientationOptionDTO body);
 *     // test course_level: livello obbligatorio, nessun esercizio consigliato;
 *     // test exercises: nessun livello, zero o più voci della Palestra della stessa materia con priorità > 0
 * OrientationOption updateOption(long optionId, OrientationOptionDTO body);
 * List&lt;OrientationOption&gt; reorderOptions(long questionId, List&lt;Long&gt; optionIds);
 * void deleteOption(long optionId);
 *     // un'opzione già scelta in un risultato salvato non si cancella (§8.4): ConflictException
 * List&lt;String&gt; checkReadiness(long testId);
 *     // Proposta §8.2: almeno 3 domande, ogni domanda con almeno 2 opzioni
 * </pre>
 * <b>Studente (Fase 3)</b>
 * <pre>
 * OrientationTestViewDTO getTestForStudent(String subjectSlug, OrientationTestKind kind);
 *     // domande e opzioni senza livelli né esercizi consigliati (il risultato non si indovina)
 * OrientationResult submit(long studentId, String subjectSlug, OrientationTestKind kind,
 *                          Map&lt;Long, Long&gt; answers);
 *     // tutte le domande obbligatorie, una opzione per domanda, opzioni del test -> altrimenti BadRequest.
 *     // course_level: il livello più basso scelto almeno due volte; se nessuno si ripete, il più basso (§8.2).
 *     // exercises: al massimo 5 voci Palestra visibili della materia; prima quelle consigliate da più
 *     // risposte, poi priorità più bassa, poi ID (§8.1). Zero voci: risultato vuoto (TODO 3 del package-info).
 *     // Salva sempre answers e risultato in orientation_results.
 * Optional&lt;OrientationResult&gt; findLastResult(long studentId, String subjectSlug, OrientationTestKind kind);
 *     // per mostrare "il tuo ultimo risultato" e per i consigli futuri
 * </pre>
 * Entity da creare: OrientationResult; campi da mappare: OrientationTest.kind, OrientationOption.level.
 * Enum da creare: OrientationTestKind (exercises, course_level). Il livello usa CourseLevel.
 */
public interface IOrientationService {
}
