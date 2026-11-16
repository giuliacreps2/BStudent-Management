package giuliacrepaldi.BStudent_Management.services.progression.interfaces;

/**
 * Letture per "I miei progressi" (blueprint §10.5).
 * Storico dei tentativi valutati, record, accrediti, totali skill ed esercizi in revisione.
 * Solo lettura: compone DTO dai fatti già persistiti, senza tabelle dedicate.
 *
 * <h2>TODO — metodi da implementare (Fase 3 la struttura, i dati con il Blocco 5 e la gamification)</h2>
 * La UI della pagina è ancora da disegnare: i metodi seguono i cinque gruppi decisi il 14/11/2026.
 * Ogni gruppo è un riassunto; il dettaglio si apre a richiesta.
 * <pre>
 * GrowthDTO getGrowth(long studentId);
 *     // personaggio e XP (uguali per tutte le materie), cosa manca alla prossima evoluzione, accessori
 *     // vicini -&gt; gamification; livelli delle skill per materia
 * List&lt;CourseProgressDTO&gt; getCourses(long studentId, String subjectSlug);
 *     // missioni superate su totali, media del corso (migliori risultati), completato
 * PracticeProgressDTO getPractice(long studentId, String subjectSlug);
 *     // record per voce, punti su massimi, tentativi; esercizi in correzione separati
 * Page&lt;AttemptHistoryItemDTO&gt; getHistory(long studentId, HistoryFilter filter, int page, int size);
 *     // ultimi 30 giorni di default, poi a richiesta; filtri Palestra/missioni e superati sì/no;
 *     // voto mostrato al mezzo punto (§10.2)
 * AttemptDetailDTO getAttempt(long studentId, long exerciseAttemptId);
 *     // risposte, feedback e "rileggi la teoria" (argomento dell'esercizio -&gt; lezioni e Risorse)
 * WeakPointsDTO getWeakPoints(long studentId, String subjectSlug);
 *     // skill più bassa, argomenti con esercizi sotto il 60%, ultimi risultati dei test
 * </pre>
 * Il selettore della materia compare solo se lo studente ne segue più di una (frontend).
 * Tempo di studio: IStudyEventsService, facoltativo nell'MVP.
 */
public interface IProgressQueryService {
}
