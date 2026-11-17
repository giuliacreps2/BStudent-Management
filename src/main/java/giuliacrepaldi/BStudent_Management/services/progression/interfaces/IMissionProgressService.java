package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

/**
 * Progressione delle missioni (blueprint principale §9.3, §10.3; Blueprint Svolgimento §5).
 * Risultato dell'esercizio: media ponderata delle skill. Punti skill con il fattore delle missioni (10%),
 * conservati anche se la missione non è superata; rifare un esercizio accredita solo la differenza rispetto
 * al proprio risultato migliore, skill per skill. XP solo al primo superamento della missione.
 *
 * <h2>TODO — metodi da implementare (Blocco 5)</h2>
 * <pre>
 * // Calcolo puro, con i test (utilities.MissionPoints):
 * static BigDecimal skillPoints(BigDecimal maxPoints, BigDecimal weight, BigDecimal percentage, BigDecimal factor);
 *     // 10 punti, quota 70, 80%, fattore 0,10 -&gt; 0,56
 *
 * void onExerciseAssessed(long studentId, long exerciseAttemptId);
 *     // dopo ogni giudizio di un esercizio di missione, superato o no: per ogni skill dell'esercizio
 *     // accredita la differenza positiva rispetto al migliore contributo già ottenuto in quell'esercizio
 *     // (ISkillCreditsService.credit, origine "mission", materia del corso); aggiorna
 *     // mission_skill_contributions; poi IStudentXpService.onSkillPointsCredited
 * MissionPassedDTO onMissionPassed(long studentId, long missionAttemptId);
 *     // percorso superato; mission_records con il risultato migliore (media degli esercizi; un risultato
 *     // peggiore in una ripetizione non lo abbassa); XP al primo superamento (IStudentXpService.onMissionPassed);
 *     // poi ICourseProgressService.onMissionPassed. Restituisce XP ottenuti, stadi nuovi, corso completato
 * </pre>
 */
public interface IMissionProgressService {
}
