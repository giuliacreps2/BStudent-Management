package giuliacrepaldi.BStudent_Management.services.resources.interfaces;

/**
 * Risorse, lato Admin (blueprint §7.4, tabelle resources e resource_links della v0.9).
 * Strumenti di ripasso della Palestra (tabelle dei verbi, declinazioni, schemi): il "libro", non escono
 * dalla Palestra e si vedono solo con l'account. Ogni risorsa è free o premium.
 * Stati draft/published/withdrawn come gli altri contenuti; indirizzo unico nella materia.
 * Le letture dello studente stanno in services.student (IStudentResourcesService).
 *
 * <h2>TODO — metodi da implementare (Fase 1, Admin)</h2>
 * <pre>
 * Resource saveNewResource(long subjectId, ResourceDTO body);
 *     // indirizzo da Slugs.resolve, unico nella materia; argomento facoltativo della stessa materia
 * Resource findById(long resourceId);
 * Page&lt;Resource&gt; findAll(Long subjectId, ResourceCategory category, ContentStatus status,
 *                        int page, int size, String sortBy);
 * Resource findByIdAndUpdate(long resourceId, ResourceDTO body);
 *     // ammessa in ogni stato; content validato con ContentBlocks (formato a blocchi unico)
 * Resource publishResource(long resourceId);    // titolo, descrizione e almeno una sezione con contenuto
 * Resource withdrawResource(long resourceId);   // i segnalibri restano ma la risorsa non si vede più
 * void deleteResource(long resourceId);         // solo se mai pubblicata
 * </pre>
 * Campi: categoria (elenco fisso: grammatica, verbi, traduzione, vocabolario), titolo, descrizione,
 * "In breve" (testo + esempio facoltativo), copertina, content (JSON: varianti della tabella, approfondimenti
 * a blocchi, esempi), argomento, free/premium, "più usata" con icona (scelta dall'autrice finché non c'è
 * l'uso reale), "in evidenza".
 * <p>
 * resource_links ("Approfondisci", origine manual/automatic): nessun metodo nell'MVP, la sezione non è
 * visibile. TODO dopo l'MVP: gestione dei collegamenti e proposta automatica (package-info di services.orientation).
 * <p>
 * Entity da creare: Resource, ResourceLink. Enum: ResourceCategory. DTO: ResourceDTO, ResourceResponseDTO.
 * Utility da creare: ContentBlocks (validazione del formato a blocchi, condivisa con le pagine della lezione).
 * Controller: /admin/academy/subjects/{subjectId}/resources e /admin/academy/resources/{resourceId}.
 */
public interface IResourcesService {
}
