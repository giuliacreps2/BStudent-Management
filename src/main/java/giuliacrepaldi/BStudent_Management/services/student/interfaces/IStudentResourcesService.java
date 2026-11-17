package giuliacrepaldi.BStudent_Management.services.student.interfaces;

import giuliacrepaldi.BStudent_Management.payloads.student.ResourceCardDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.ResourceDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.student.ResourcesHubDTO;

import java.util.List;

/**
 * Risorse viste dallo studente (blueprint §7.4; frontend types/resources.ts, types/resourceDetail.ts).
 * Solo con account; free o premium. Le pagine pubbliche con SEO restano solo come copia di studio nel
 * frontend (app/SEO_criteria). Segnalibri: INotebookService. Tempo di lettura: IStudyEventsService.
 */
public interface IStudentResourcesService {

    // subjectSlug null o non valido: la prima materia pubblicata
    ResourcesHubDTO getHub(long studentId, String subjectSlug);

    ResourceDetailDTO getResource(long studentId, String subjectSlug, String resourceSlug);

    // Solo Risorse visibili della materia, per titolo e descrizione; al massimo 20
    List<ResourceCardDTO> search(long studentId, String subjectSlug, String query);
}
