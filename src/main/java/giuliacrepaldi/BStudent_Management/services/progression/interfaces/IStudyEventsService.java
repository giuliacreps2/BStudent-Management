package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

/**
 * Registro di studio (blueprint §10.5, tabella study_events della v0.9).
 * Dati raccolti da subito per gli algoritmi futuri (consigli per lo studente, statistiche anonime della
 * piattaforma); l'algoritmo arriva dopo la beta. Si aggiunge e non si modifica. I tentativi degli
 * esercizi hanno già i loro orari e non passano da qui.
 *
 * <h2>TODO — metodi da implementare (Fase 3, studente)</h2>
 * <pre>
 * void recordLessonReading(long studentId, long lessonId, Long lessonPageId,
 *                          OffsetDateTime startedAt, long activeMs);
 *     // un tratto di lettura: il frontend somma il tempo attivo (segnale ogni 30 s, stop dopo 10 minuti
 *     // senza interazione o con la scheda nascosta) e lo invia a tratti. Lezione visibile allo studente;
 *     // activeMs &gt; 0 e non più lungo del tratto; startedAt non nel futuro
 * void recordResourceReading(long studentId, long resourceId, OffsetDateTime startedAt, long activeMs);
 * void recordRecommendationOpened(long studentId, long practiceExerciseId);
 * StudyTimeDTO getStudyTime(long studentId, LocalDate from, LocalDate to);
 *     // tempo di studio = teoria (eventi) + esercizi (inizio -> consegna dei tentativi, limitato al tempo
 *     // concesso o a 60 minuti); minuti per giorno di attività, ora italiana. Facoltativo nell'MVP
 * </pre>
 * Statistiche della piattaforma solo aggregate e anonime (TODO legale nel package-info di services.orientation).
 * Entity da creare: StudyEvent. Controller: POST /student/study-events.
 */
public interface IStudyEventsService {
}
