package giuliacrepaldi.BStudent_Management.services.student.interfaces;

/**
 * Palestra vista dallo studente, "Esercizi" nel menu (blueprint §7.3; frontend types/exercises.ts).
 * Solo voci della Palestra (practice_exercises): gli id mandati al frontend sono quelli delle voci.
 * Gli esercizi e i tentativi delle missioni non compaiono qui.
 *
 * <h2>TODO — metodi da implementare (Fase 3)</h2>
 * <pre>
 * ExercisesPageDTO getExercisesPage(long studentId, String subjectSlug);
 *     // materia attiva (scelta dal frontend, default l'ultima usata); skill della materia nell'ordine
 *     // dell'autrice (codice, nome, descrizione, icona, colore); attività da riprendere (completato &gt;= 60%,
 *     // salvato, da rifare &lt; 60%, in correzione) e sfida "supera 3 oggi" (ora italiana, ogni voce una
 *     // volta, nessun premio): vuote finché il Blocco 5 non registra tentativi; bonus = lista vuota
 * PracticeListDTO getPracticeBySkill(long studentId, String subjectSlug, String skillCode);
 *     // voci visibili della skill, raggruppate per argomento: titolo per lo studente, immagine
 *     // (argomento, altrimenti skill), tempo stimato se c'è, punti massimi, stato personale, lucchetto premium
 * List&lt;PracticeItemDTO&gt; getTodayGoal(long studentId, String subjectSlug, String skillCode);
 *     // "Obiettivo di oggi": skillCode null = "Sorprendimi" (skill più debole). 3 voci: prima da rifare,
 *     // poi mai svolte, poi le altre; escluse le bloccate (premium senza accesso). Regola spiegabile:
 *     // primo livello della personalizzazione (package-info di services.orientation)
 * List&lt;PracticeItemDTO&gt; getByTopic(long studentId, long topicId);
 *     // "Vai agli esercizi" dalle Risorse e "rileggi la teoria" al contrario; vuota = pulsante nascosto
 * </pre>
 * Controller: /student/practice/**.
 */
public interface IStudentPracticeService {
}
