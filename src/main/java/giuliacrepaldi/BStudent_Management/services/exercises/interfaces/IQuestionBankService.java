package giuliacrepaldi.BStudent_Management.services.exercises.interfaces;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionVersion;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionBankEntryDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionBankEntryListItemDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionVersionDTO;
import giuliacrepaldi.BStudent_Management.payloads.exercises.QuestionVersionResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Esercizi editoriali: identità, versioni, domande e risposte (blueprint §6.1, §6.2).
 * Numero di versione unico per esercizio; un nuovo tentativo risolve la versione disponibile.
 * Le letture destinate allo studente non espongono le soluzioni prima della consegna.
 * Operazioni editoriali riservate all'Admin.
 */
public interface IQuestionBankService {

    //Admin
    //CRUD -> create, read, update, delete

    QuestionBankEntry saveNewQuestionBankEntry(QuestionBankEntryDTO body); // nasce draft, senza versioni

    QuestionBankEntry findById(long questionBankEntryId);

    Page<QuestionBankEntry> findAll(int page, int size, String sortBy);

    // Elenco dell'Admin con data della prima versione e versione in uso (da quando)
    Page<QuestionBankEntryListItemDTO> findAllWithVersionInfo(int page, int size, String sortBy);

    QuestionBankEntry findByIdAndUpdate(long questionBankEntryId, QuestionBankEntryDTO body); // titolo interno

    QuestionBankEntry publishQuestionBankEntry(long questionBankEntryId); // solo con una versione pubblicata

    QuestionBankEntry withdrawQuestionBankEntry(long questionBankEntryId); // published -> withdrawn

    void deleteQuestionBankEntry(long questionBankEntryId); // solo draft mai pubblicato e mai collegato

    //Versioni: domanda, risposte e rubriche viaggiano insieme nella versione

    List<QuestionVersion> findVersions(long questionBankEntryId); // per numero di versione

    QuestionVersion findVersionById(long questionVersionId);

    // Versione con domanda, risposte e rubriche: vista editoriale, include le soluzioni
    QuestionVersionResponseDTO findVersionDetails(long questionVersionId);

    QuestionVersion createDraftVersion(long questionBankEntryId, QuestionVersionDTO body); // numero = ultimo + 1

    // Solo bozze: una versione pubblicata o già svolta non si modifica (ConflictException)
    QuestionVersion updateDraftVersion(long questionVersionId, QuestionVersionDTO body);

    // Toglie il flag alla versione pubblicata precedente, poi lo mette a questa (stessa transazione)
    QuestionVersion publishVersion(long questionVersionId);

    void deleteDraftVersion(long questionVersionId); // solo bozze mai pubblicate
}
