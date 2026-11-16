package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

/**
 * Livelli delle skill e sblocco delle voci della Palestra (Blueprint Gamification §2, §3).
 * Il livello si calcola dai punti dello studente in una skill e in una materia (Palestra + missioni):
 * si parte dal livello 1 con 0 punti; soglie della beta 0, 20, 50, 95, 165, 265. Il livello non scende mai.
 * Una voce è aperta se la sua difficoltà è al massimo "livello della skill + 1", nella materia della voce.
 * Una sola scala di difficoltà in tutta la piattaforma (§3.6).
 * Livello di partenza dal test di orientamento (§2.6): vale come punti di partenza pari alla soglia del
 * livello, usati solo per il livello; per gli accessori contano i punti veri.
 * Programma (§3-bis): la classe di riferimento dell'argomento non blocca, dà solo un avviso.
 *
 * <h2>TODO — metodi da implementare (Fase D)</h2>
 * <pre>
 * // Calcolo puro, senza DB (utilities.SkillLevelCurve): è la parte coperta dai test.
 * static int levelFor(BigDecimal points, List&lt;BigDecimal&gt; thresholds);
 *     // 0 -&gt; 1; 19,99 -&gt; 1; 20 -&gt; 2; 60 -&gt; 3; 265 e oltre -&gt; 6
 * static boolean isUnlocked(int difficulty, int skillLevel);
 *     // difficulty &lt;= skillLevel + 1
 *
 * List&lt;SkillLevelStateDTO&gt; getLevels(long studentId, long subjectId);
 *     // una riga per skill pubblicata nella materia (subject_skills), nell'ordine dell'autrice:
 *     // punti, livello, soglia del livello attuale e del prossimo, percentuale della barra, "livello massimo"
 * SkillLevelStateDTO getLevel(long studentId, long subjectId, long skillId);
 * BigDecimal getAverageLevel(long studentId, long subjectId);
 *     // media semplice, non arrotondata; contano solo le skill pubblicate nella materia e con esercizi
 *     // pubblicati (§4.2). Nessuna skill valida -&gt; 1
 * BigDecimal getBestAverageLevel(long studentId);
 *     // la materia migliore tra quelle in cui lo studente ha punti (§4.2)
 * PracticeLockDTO getLock(long studentId, long practiceExerciseId);
 *     // aperta, oppure bloccata con "Si sblocca al livello N di [skill]" (N = difficoltà - 1).
 *     // Lo usa IStudentPracticeService (branch dei corsi) per l'elenco e prima di avviare un tentativo
 * Map&lt;Long, PracticeLockDTO&gt; getLocks(long studentId, Collection&lt;Long&gt; practiceExerciseIds);
 *     // stesso calcolo per un elenco, con una sola lettura dei totali
 * void setStartingLevel(long studentId, long subjectId, long skillId, int level);
 *     // lo chiama il test di orientamento (branch dei corsi); può solo salire; livello tra 1 e il massimo
 * ProgramNoticeDTO getProgramNotice(long studentId, long practiceExerciseId);
 *     // "per la tua classe" oppure "Di solito si studia in [classe]"; senza classe dello studente o
 *     // dell'argomento: nessun avviso. Le voci "più avanti" restano apribili
 *
 * // Admin
 * List&lt;SkillLevel&gt; findLevels(long skillId);
 * List&lt;SkillLevel&gt; replaceLevels(long skillId, SkillLevelsDTO body);
 *     // soglie crescenti, livello 1 = 0 punti; nel form la scelta "applica a tutte le skill".
 *     // Il livello è calcolato: aggiungere un livello in cima non cambia nulla a nessuno;
 *     // alzare una soglia esistente farebbe scendere chi l'aveva superata, quindi è vietato
 *     // (BadRequestException: "Puoi aggiungere livelli o abbassare le soglie, non alzarle")
 * </pre>
 * Fatti letti: vista student_skill_totals (studente, materia, skill, origine) sopra skill_credit_entries.
 * DTO da creare: SkillLevelStateDTO, PracticeLockDTO, SkillLevelsDTO, ProgramNoticeDTO.
 * TODO per il branch dei corsi: campo "Difficoltà" nel form Admin della voce della Palestra
 * (PracticeExerciseDTO) e uso di getLock nelle letture dello studente.
 * Controller: GET /student/skills/levels?subject=; /admin/academy/skills/{id}/levels.
 */
public interface ISkillLevelsService {
}
