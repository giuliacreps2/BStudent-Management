package giuliacrepaldi.BStudent_Management.services.practice.interfaces;

/**
 * Catalogo della Palestra, lato Admin (blueprint §7.1, §7.3).
 * Sempre accessibile, anche senza vite; ogni esercizio del banco compare al massimo una volta (UNIQUE).
 * Ogni voce ha una skill, un argomento e punti massimi impostati dall'Admin; la materia si ricava dall'argomento.
 * Stati draft/published/withdrawn e livello free/premium propri (v0.6); tempo stimato facoltativo (v0.9).
 * Se l'esercizio viene ritirato dal banco la voce sparisce anche dalla Palestra, senza ritirarla (v0.8).
 * Le letture dello studente stanno in services.student (IStudentPracticeService).
 *
 * <h2>TODO — metodi da implementare (Fase 1, Admin)</h2>
 * <pre>
 * PracticeExercise saveNewPracticeExercise(PracticeExerciseDTO body);
 *     // esercizio del banco non ancora in Palestra (altrimenti ConflictException);
 *     // la skill deve essere tra le skill della materia dell'argomento (ISubjectSkillsService)
 * PracticeExercise findById(long practiceExerciseId);
 * Page&lt;PracticeExercise&gt; findAll(Long subjectId, Long topicId, Long skillId, ContentStatus status,
 *                                int page, int size, String sortBy);
 *     // filtri semplici, tutti facoltativi; pochi esercizi nell'MVP: niente Specification
 * PracticeExercise findByIdAndUpdate(long practiceExerciseId, PracticeExerciseDTO body);
 *     // skill, argomento, punti massimi, tempo stimato, free/premium; l'esercizio del banco non cambia.
 *     // Cambiare i punti massimi vale da ora: i record già ottenuti restano (blueprint §10.1)
 * PracticeExercise publishPracticeExercise(long practiceExerciseId);
 *     // solo se l'esercizio del banco è pubblicato (quindi ha versione e titolo per lo studente)
 * PracticeExercise withdrawPracticeExercise(long practiceExerciseId);
 * void deletePracticeExercise(long practiceExerciseId);
 *     // solo se mai pubblicata e senza tentativi né consigli dei test, altrimenti va ritirata
 * </pre>
 * DTO da creare: PracticeExerciseDTO (questionBankEntryId, skillId, topicId, maxPoints, estimatedMinutes,
 * accessTier), PracticeExerciseResponseDTO (con titolo dell'esercizio e materia).
 * Controller: /admin/academy/practice-exercises.
 */
public interface IPracticeCatalogService {
}
