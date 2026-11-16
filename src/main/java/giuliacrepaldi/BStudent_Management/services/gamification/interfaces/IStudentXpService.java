package giuliacrepaldi.BStudent_Management.services.gamification.interfaces;

/**
 * XP dello studente ed evoluzioni del personaggio (Blueprint Gamification §1, §4).
 * XP = somma dei punti massimi degli esercizi della missione, accreditati solo al primo superamento.
 * Stadi della beta: Base, Evoluzione I (primo corso completato, base vite da 5 a 7), Evoluzione II
 * (800 XP e livello medio almeno 3 nella materia migliore). Uno stadio raggiunto non si perde mai.
 * XP e stadio sono dello studente: tutti i personaggi mostrano lo stesso stadio.
 *
 * <h2>TODO — metodi da implementare (Fase D)</h2>
 * <pre>
 * BigDecimal getTotalXp(long studentId);
 *     // somma di student_xp_entries
 * EvolutionProgressDTO getEvolutionProgress(long studentId);
 *     // stadio attuale, prossimo stadio e che cosa manca (XP mancanti, livello medio attuale e richiesto);
 *     // dopo l'ultimo stadio: nessun "prossimo"
 * // Calcolo puro, coperto dai test:
 * static boolean meetsStage(BigDecimal xp, BigDecimal bestAverageLevel, BigDecimal minXp, BigDecimal minAverageLevel);
 *     // entrambe le condizioni, media non arrotondata: 2,8 non vale 3 (§4.3)
 *
 * // Eventi in arrivo dal Blocco 5 (svolgimento): contratto, restano TODO finché il Blocco 5 non esiste
 * void onMissionPassed(long studentId, long missionId, long missionAttemptId);
 *     // primo superamento: scrive una riga in student_xp_entries (XP = somma dei punti massimi degli
 *     // esercizi visibili in quel momento); ripetizioni e doppio invio: nessun effetto (UNIQUE studente +
 *     // missione). Poi chiama evaluateEvolutions
 * void onSkillPointsCredited(long studentId);
 *     // dopo ogni accredito di punti skill (Palestra o missione): chiama evaluateEvolutions
 * void onCourseCompleted(long studentId, long courseId);
 *     // primo corso completato: Evoluzione I una sola volta (student_first_evolutions),
 *     // poi ILivesService.raiseBaseLives e evaluateEvolutions
 * List&lt;Integer&gt; evaluateEvolutions(long studentId);
 *     // assegna in ordine gli stadi con soglia di cui lo studente ha i requisiti e restituisce quelli nuovi
 *     // (per il messaggio "Il tuo personaggio si è evoluto"); l'Evoluzione II richiede la I (§4.4)
 * </pre>
 * Fatti letti: student_xp_entries, student_evolution_stages, student_first_evolutions, soglie degli stadi,
 * ISkillLevelsService.getBestAverageLevel.
 * DTO da creare: EvolutionProgressDTO. Entity da creare: StudentXpEntry, StudentEvolutionStage.
 */
public interface IStudentXpService {
}
