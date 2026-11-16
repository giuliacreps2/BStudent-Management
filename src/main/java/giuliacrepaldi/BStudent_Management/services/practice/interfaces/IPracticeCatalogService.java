package giuliacrepaldi.BStudent_Management.services.practice.interfaces;

import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.payloads.practice.PracticeExerciseDTO;
import org.springframework.data.domain.Page;

/**
 * Catalogo della Palestra, lato Admin (blueprint §7.1, §7.3).
 * Sempre accessibile, anche senza vite; ogni esercizio del banco compare al massimo una volta (UNIQUE).
 * Ogni voce ha una skill, un argomento e punti massimi impostati dall'Admin; la materia si ricava dall'argomento.
 * Stati draft/published/withdrawn e livello free/premium propri (v0.6); tempo stimato facoltativo (v0.9).
 * Se l'esercizio viene ritirato dal banco la voce sparisce anche dalla Palestra, senza ritirarla (v0.8).
 * Le letture dello studente stanno in services.student (IStudentPracticeService).
 * <p>
 * - La skill deve essere tra le skill della materia dell'argomento (ISubjectSkillsService).
 * - Cambiare i punti massimi vale da ora: i record già ottenuti restano (§10.1).
 * - publish: solo se l'esercizio del banco è pubblicato (quindi con versione e titolo per lo studente).
 * - delete: solo se la voce non è mai stata pubblicata e nessun test la consiglia; altrimenti va ritirata.
 */
public interface IPracticeCatalogService {

    //Admin
    //CRUD -> create, read, update, delete

    PracticeExercise saveNewPracticeExercise(PracticeExerciseDTO body); // nasce draft

    PracticeExercise findById(long practiceExerciseId);

    // Filtri facoltativi (null = tutti)
    Page<PracticeExercise> findAll(Long subjectId, Long topicId, Long skillId, ContentStatus status,
                                   int page, int size, String sortBy);

    PracticeExercise findByIdAndUpdate(long practiceExerciseId, PracticeExerciseDTO body);

    PracticeExercise publishPracticeExercise(long practiceExerciseId);

    PracticeExercise withdrawPracticeExercise(long practiceExerciseId);

    void deletePracticeExercise(long practiceExerciseId);
}
