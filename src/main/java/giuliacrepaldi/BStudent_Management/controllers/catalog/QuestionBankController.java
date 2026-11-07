package giuliacrepaldi.BStudent_Management.controllers.catalog;

import giuliacrepaldi.BStudent_Management.payloads.exercises.*;
import giuliacrepaldi.BStudent_Management.services.exercises.interfaces.IQuestionBankService;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**. Le risposte includono le soluzioni
@RestController
@RequestMapping("/admin/academy")
public class QuestionBankController {

    private final IQuestionBankService questionBankService;

    public QuestionBankController(IQuestionBankService questionBankService) {
        this.questionBankService = questionBankService;
    }

    //ESERCIZI: nascono draft e senza versioni
    @PostMapping("/exercises")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionBankEntryResponseDTO create(@RequestBody @Valid QuestionBankEntryDTO body) {
        return QuestionBankEntryResponseDTO.from(questionBankService.saveNewQuestionBankEntry(body));
    }

    // Con data della prima versione e versione in uso (da quando); sortBy ammette anche createdAt e updatedAt
    @GetMapping("/exercises")
    public PagedModel<QuestionBankEntryListItemDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size,
                                                            @RequestParam(defaultValue = "internalQuestionTitle") String sortBy) {
        return new PagedModel<>(questionBankService.findAllWithVersionInfo(page, size, sortBy));
    }

    @GetMapping("/exercises/{exerciseId}")
    public QuestionBankEntryResponseDTO findById(@PathVariable long exerciseId) {
        return QuestionBankEntryResponseDTO.from(questionBankService.findById(exerciseId));
    }

    @PutMapping("/exercises/{exerciseId}")
    public QuestionBankEntryResponseDTO update(@PathVariable long exerciseId, @RequestBody @Valid QuestionBankEntryDTO body) {
        return QuestionBankEntryResponseDTO.from(questionBankService.findByIdAndUpdate(exerciseId, body));
    }

    // 409 senza una versione pubblicata
    @PatchMapping("/exercises/{exerciseId}/publish")
    public QuestionBankEntryResponseDTO publish(@PathVariable long exerciseId) {
        return QuestionBankEntryResponseDTO.from(questionBankService.publishQuestionBankEntry(exerciseId));
    }

    // Dove è usato: il frontend lo mostra prima del ritiro e avvisa per le missioni che resterebbero senza esercizi
    @GetMapping("/exercises/{exerciseId}/usages")
    public QuestionBankEntryUsagesDTO findUsages(@PathVariable long exerciseId) {
        return questionBankService.findUsages(exerciseId);
    }

    // Ammesso anche se usato in missioni pubblicate (sparisce dalla sequenza); 409 se pubblicato in Palestra
    @PatchMapping("/exercises/{exerciseId}/withdraw")
    public QuestionBankEntryResponseDTO withdraw(@PathVariable long exerciseId) {
        return QuestionBankEntryResponseDTO.from(questionBankService.withdrawQuestionBankEntry(exerciseId));
    }

    // Solo bozze mai pubblicate e non collegate; le versioni in bozza vengono eliminate con l'esercizio
    @DeleteMapping("/exercises/{exerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long exerciseId) {
        questionBankService.deleteQuestionBankEntry(exerciseId);
    }

    //VERSIONI DELL'ESERCIZIO: il contenuto (domanda, risposte, rubriche) sta nella versione
    @PostMapping("/exercises/{exerciseId}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionVersionResponseDTO createVersion(@PathVariable long exerciseId,
                                                    @RequestBody @Valid QuestionVersionDTO body) {
        long versionId = questionBankService.createDraftVersion(exerciseId, body).getQuestionVersionId();
        return questionBankService.findVersionDetails(versionId);
    }

    // Senza contenuto: il dettaglio è su /versions/{versionId}
    @GetMapping("/exercises/{exerciseId}/versions")
    public List<QuestionVersionSummaryDTO> findVersions(@PathVariable long exerciseId) {
        return questionBankService.findVersions(exerciseId).stream().map(QuestionVersionSummaryDTO::from).toList();
    }

    //VERSIONI
    @GetMapping("/versions/{versionId}")
    public QuestionVersionResponseDTO findVersion(@PathVariable long versionId) {
        return questionBankService.findVersionDetails(versionId);
    }

    // Solo bozze mai pubblicate né svolte, altrimenti 409: per correggere si crea una nuova versione
    @PutMapping("/versions/{versionId}")
    public QuestionVersionResponseDTO updateVersion(@PathVariable long versionId,
                                                    @RequestBody @Valid QuestionVersionDTO body) {
        questionBankService.updateDraftVersion(versionId, body);
        return questionBankService.findVersionDetails(versionId);
    }

    // Sostituisce la versione pubblicata precedente: i nuovi tentativi useranno questa
    @PatchMapping("/versions/{versionId}/publish")
    public QuestionVersionSummaryDTO publishVersion(@PathVariable long versionId) {
        return QuestionVersionSummaryDTO.from(questionBankService.publishVersion(versionId));
    }

    @DeleteMapping("/versions/{versionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVersion(@PathVariable long versionId) {
        questionBankService.deleteDraftVersion(versionId);
    }
}
