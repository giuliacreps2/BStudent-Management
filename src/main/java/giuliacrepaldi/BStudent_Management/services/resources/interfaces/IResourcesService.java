package giuliacrepaldi.BStudent_Management.services.resources.interfaces;

import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.resources.ResourceCategory;
import giuliacrepaldi.BStudent_Management.payloads.resources.ResourceDTO;
import org.springframework.data.domain.Page;

/**
 * Risorse, lato Admin (blueprint §7.4, tabelle resources e resource_links della v0.9).
 * Strumenti di ripasso della Palestra (tabelle dei verbi, declinazioni, schemi): il "libro", non escono
 * dalla Palestra e si vedono solo con l'account. Ogni risorsa è free o premium.
 * Stati draft/published/withdrawn come gli altri contenuti; indirizzo unico nella materia.
 * Le letture dello studente stanno in services.student (IStudentResourcesService).
 * <p>
 * - content (summaryExample, tool, insights, examples) è validato con ContentBlocks a ogni salvataggio.
 * - Argomento facoltativo, della stessa materia; "più usata" sempre insieme alla sua icona.
 * - publish: titolo, descrizione e almeno una sezione con contenuto (tabella, approfondimenti o esempi).
 * - withdraw: i segnalibri restano ma la risorsa non si vede più. delete: solo se mai pubblicata.
 * - resource_links ("Approfondisci", origine manual/automatic): nessun metodo nell'MVP, la sezione non è
 *   visibile. TODO dopo l'MVP: gestione dei collegamenti e proposta automatica (package-info di services.orientation).
 */
public interface IResourcesService {

    //Admin
    //CRUD -> create, read, update, delete

    Resource saveNewResource(long subjectId, ResourceDTO body); // nasce draft

    Resource findById(long resourceId);

    // Filtri facoltativi (null = tutti)
    Page<Resource> findAll(Long subjectId, ResourceCategory category, ContentStatus status,
                           int page, int size, String sortBy);

    Resource findByIdAndUpdate(long resourceId, ResourceDTO body); // ammessa in ogni stato

    Resource publishResource(long resourceId);

    Resource withdrawResource(long resourceId);

    void deleteResource(long resourceId);
}
