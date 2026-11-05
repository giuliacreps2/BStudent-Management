package giuliacrepaldi.BStudent_Management.services.catalog.interfaces;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.payloads.catalog.SubjectDTO;
import org.springframework.data.domain.Page;

/**
 * Materie (blueprint §5.1).
 * Nessuno stato editoriale: la materia esiste o non esiste.
 * saveNewSubject, findById, findAll (con Page), findByIdAndUpdate: nome e skill predefinita, che fa da fallback per i pesi.
 * - deleteSubject: solo se la materia non ha corsi.
 * - Ho tolto archive e patch: la materia non ha uno stato.
 */
public interface ISubjectsService {

    //Admin
    //CRUD -> create, read, update, delete

    Subject saveNewSubject(SubjectDTO body);

    Subject findById(long subjectId);

    Page<Subject> findAll(int page, int size, String sortBy);

    Subject findByIdAndUpdate(long subjectId, SubjectDTO body); // nome e skill predefinita (fallback dei pesi)

    void deleteSubject(long subjectId); // solo senza corsi, altrimenti ConflictException
}
