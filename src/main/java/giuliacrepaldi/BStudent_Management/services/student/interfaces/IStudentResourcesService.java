package giuliacrepaldi.BStudent_Management.services.student.interfaces;

/**
 * Risorse viste dallo studente (blueprint §7.4; frontend types/resources.ts, types/resourceDetail.ts).
 * Solo con account; free o premium. Le pagine pubbliche con SEO restano solo come copia di studio nel
 * frontend (app/SEO_criteria).
 *
 * <h2>TODO — metodi da implementare (Fase 3)</h2>
 * <pre>
 * ResourcesHubDTO getHub(long studentId, String subjectSlug);
 *     // "Le risorse più usate" (scelte dall'autrice, con icona), categorie con la loro descrizione,
 *     // "in evidenza", "Percorso consigliato" verso il test di orientamento. Testi della hero statici
 * ResourceDetailDTO getResource(long studentId, String subjectSlug, String resourceSlug);
 *     // sezioni presenti solo se hanno contenuto; lucchetto premium; isSaved = segnalibro nel Quaderno;
 *     // "Vai agli esercizi" dall'argomento (nascosto senza voci visibili); "Approfondisci" NON restituito
 * List&lt;ResourceSearchResultDTO&gt; search(long studentId, String subjectSlug, String query);
 *     // solo Risorse visibili della materia, titolo e descrizione, ricerca semplice (ILIKE), massimo 20
 * </pre>
 * Segnalibri: INotebookService. Tempo di lettura: IStudyEventsService.
 * Controller: /student/resources/**.
 */
public interface IStudentResourcesService {
}
