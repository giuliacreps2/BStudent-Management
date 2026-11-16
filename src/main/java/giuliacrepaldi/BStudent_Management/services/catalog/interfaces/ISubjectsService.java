package giuliacrepaldi.BStudent_Management.services.catalog.interfaces;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.payloads.catalog.SubjectDTO;
import org.springframework.data.domain.Page;

/**
 * Materie (blueprint §5.1, §5.8).
 * Stato (v0.9): bozza = nascosta agli studenti, pubblicata = visibile. Una materia pubblicata senza
 * corsi pubblicati appare come "Presto disponibile" (calcolato nella lettura studente).
 * saveNewSubject, findById, findAll (con Page), findByIdAndUpdate: nome, indirizzo, tagline, immagine,
 * guida del briefing e skill predefinita, che fa da fallback per i pesi.
 * - publishSubject / unpublishSubject: mostra o nasconde la materia.
 * - deleteSubject: solo se la materia non ha corsi.
 */
public interface ISubjectsService {

    //Admin
    //CRUD -> create, read, update, delete

    Subject saveNewSubject(SubjectDTO body);

    Subject findById(long subjectId);

    Page<Subject> findAll(int page, int size, String sortBy);

    Subject findByIdAndUpdate(long subjectId, SubjectDTO body);

    Subject publishSubject(long subjectId);

    Subject unpublishSubject(long subjectId); // torna in bozza: nascosta agli studenti

    void deleteSubject(long subjectId); // solo senza corsi, altrimenti ConflictException
}
