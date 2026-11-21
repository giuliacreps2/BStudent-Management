package giuliacrepaldi.BStudent_Management.payloads.progression;

import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseOutcomeDTO;
import giuliacrepaldi.BStudent_Management.payloads.attempts.ExerciseViewDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.CharacterDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionProgressDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelStateDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.StudentAccessoryDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.PracticeItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.StudentTestResultDTO;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * "I miei progressi" (blueprint §10.5): cinque gruppi, ognuno un riassunto con il dettaglio a richiesta.
 * Solo lettura: compone i fatti già salvati.
 */
public final class ProgressDTOs {

    private ProgressDTOs() {
    }

    // 1. Crescita: personaggio e XP (uguali per tutte le materie), cammino verso la prossima evoluzione,
    // accessori vicini, livelli delle skill per materia. character è null se non ci sono personaggi
    public record GrowthDTO(EvolutionProgressDTO evolution, CharacterDTO character,
                            List<SubjectSkillsDTO> subjects, List<StudentAccessoryDTO> nearAccessories) {
    }

    public record SubjectSkillsDTO(String subjectSlug, String subjectName, List<SkillLevelStateDTO> levels) {
    }

    // 2. Corsi: missioni superate su quelle visibili, media dei migliori risultati (null se nessuna missione
    // superata), completato
    public record CourseProgressDTO(String courseSlug, String courseTitle, int passedMissions, int totalMissions,
                                    BigDecimal percentage, boolean completed) {
    }

    // 3. Palestra: voci svolte con il miglior risultato, i tentativi valutati e l'ultimo (per rivedere la
    // correzione); le voci in correzione sono separate dai risultati definitivi
    public record PracticeProgressDTO(List<PracticeProgressItemDTO> items, List<PracticeItemDTO> inReview) {
    }

    public record PracticeProgressItemDTO(PracticeItemDTO item, BigDecimal bestPercentage, int assessedAttempts,
                                          Long lastAssessedAttemptId) {
    }

    // 4. Storico: un tentativo consegnato. status: assessed (con esito), pending_review (in correzione),
    // expired (tempo finito in missione). grade = voto in decimi al mezzo punto (§10.2), solo con esito.
    // teacher = corretto dalla docente
    public record AttemptHistoryItemDTO(Long exerciseAttemptId, ExerciseContext context, String title, String place,
                                        String subjectName, OffsetDateTime date, String status,
                                        BigDecimal percentage, BigDecimal grade, Boolean passed, boolean teacher) {
    }

    // Dettaglio: l'esercizio come l'ha visto, la risposta data e l'esito con il feedback (della docente se
    // l'ha corretto). Soluzione secondo le regole dello svolgimento: mai dopo un errore in missione (§3)
    public record AttemptDetailDTO(AttemptHistoryItemDTO attempt, ExerciseViewDTO exercise, String answer,
                                   ExerciseOutcomeDTO outcome) {
    }

    // 5. Punti deboli: la skill più bassa, gli argomenti con esercizi non ancora superati (con la Risorsa da
    // rileggere) e gli ultimi risultati dei test della materia
    public record WeakPointsDTO(SkillLevelStateDTO weakestSkill, List<WeakTopicDTO> topics,
                                StudentTestResultDTO lastLevelTest, StudentTestResultDTO lastOrientationTest) {
    }

    public record WeakTopicDTO(Long topicId, String topicName, int exercisesToReview, String subjectSlug,
                               String resourceSlug, String resourceTitle) {
    }
}
