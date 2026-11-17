package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

/**
 * Record e punti della Palestra (blueprint principale §10.1).
 * Contributo = punti massimi x percentuale / 100, a due decimali, solo da 60%.
 * Un nuovo record accredita soltanto la differenza positiva rispetto al contributo acquisito.
 *
 * <h2>TODO — metodi da implementare (Blocco 5)</h2>
 * <pre>
 * PracticeRewardsDTO onAssessed(long studentId, long exerciseAttemptId);
 *     // dopo ogni giudizio di un tentativo della Palestra (automatico o della docente):
 *     // 1. sotto la soglia: solo storico, nessun accredito;
 *     // 2. nuovo record: aggiorna practice_records e accredita la differenza nella skill della voce,
 *     //    nella materia dell'argomento (ISkillCreditsService.credit);
 *     // 3. vite del tris e della sfida, Serie e pausa (IDailyGoalsService.onPracticeAttemptEvaluated);
 *     // 4. evoluzioni (IStudentXpService.onSkillPointsCredited).
 *     // Ripetuta per lo stesso tentativo: nessun doppio effetto
 * </pre>
 * Esempio (§10.1): massimo 10. Primo 87% -&gt; +8,70. Poi 89% -&gt; +0,20. Poi 80% -&gt; nulla. Poi 100% -&gt; +1,10.
 */
public interface IPracticeProgressService {
}
