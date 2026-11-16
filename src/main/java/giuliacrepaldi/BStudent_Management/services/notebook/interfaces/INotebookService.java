package giuliacrepaldi.BStudent_Management.services.notebook.interfaces;

/**
 * Quaderno dello studente (blueprint §14; "Taccuino" nel blueprint, "Quaderno" nell'interfaccia).
 * Sono gli appunti, non il libro: lezioni delle missioni (anche video) con la nota personale e articoli
 * del Blog preferiti. Le Risorse salvate sono solo segnalibri, separati dagli appunti (§7.4).
 *
 * <h2>TODO — metodi da implementare (Fase 3, studente)</h2>
 * <pre>
 * NotebookOverviewDTO getOverview(long studentId);
 *     // linguette = materie delle lezioni acquisite + "Articoli"; per ogni voce: tipo (Lezione/Video/Articolo),
 *     // argomento, titolo, anteprima (primo paragrafo), data di ingresso, missione, nuova (mai riaperta,
 *     // dagli eventi di studio), ha una nota. Lezioni ritirate: resta solo la nota con "lezione non più
 *     // disponibile". Premium scaduto: titolo, anteprima e nota, lezione con lucchetto (IEntitlementService).
 *     // Più il colore della copertina e i segnalibri.
 * void acquireLesson(long studentId, long lessonId);
 *     // "Ho finito di leggere" o fine del video; lezione visibile allo studente; già presente -> nessun
 *     // errore e nessun duplicato. Lo studente non può toglierla.
 * NotebookLessonDTO getLesson(long studentId, long lessonId);
 *     // lezione in sola lettura + nota; 404 se non è nel Quaderno
 * LessonNote saveNote(long studentId, long lessonId, LessonNoteDTO body);
 *     // salvataggio automatico; body con la versione letta: se nel frattempo è cambiata -> ConflictException
 *     // (due schede aperte); testo vuoto = cancellare la nota
 * void deleteNote(long studentId, long lessonId);
 * void setCoverColor(long studentId, String color);
 *     // chiave della tavolozza del frontend, null = predefinito
 * void addBookmark(long studentId, long resourceId);     // risorsa visibile allo studente; idempotente
 * void removeBookmark(long studentId, long resourceId);
 * </pre>
 * Preferiti del Blog: con il Blog (v0.10), non in questo branch.
 * Entity da creare: ResourceBookmark; campo da mappare: Student.notebookCoverColor.
 * Controller: /student/notebook/**.
 */
public interface INotebookService {
}
