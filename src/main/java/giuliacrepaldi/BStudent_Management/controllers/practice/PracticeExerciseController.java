package giuliacrepaldi.BStudent_Management.controllers.practice;

import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.payloads.practice.PracticeExerciseDTO;
import giuliacrepaldi.BStudent_Management.payloads.practice.PracticeExerciseResponseDTO;
import giuliacrepaldi.BStudent_Management.services.practice.interfaces.IPracticeCatalogService;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**
@RestController
@RequestMapping("/admin/academy/practice-exercises")
public class PracticeExerciseController {

    private final IPracticeCatalogService practiceCatalogService;

    public PracticeExerciseController(IPracticeCatalogService practiceCatalogService) {
        this.practiceCatalogService = practiceCatalogService;
    }

    // Body: { "questionBankEntryId": 1, "skillId": 2, "topicId": 3, "maxPoints": 10, "estimatedMinutes": 5, "accessTier": "free" }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PracticeExerciseResponseDTO create(@RequestBody @Valid PracticeExerciseDTO body) {
        return PracticeExerciseResponseDTO.from(practiceCatalogService.saveNewPracticeExercise(body));
    }

    // Filtri facoltativi: materia, argomento, skill, stato
    @GetMapping
    public PagedModel<PracticeExerciseResponseDTO> findAll(@RequestParam(required = false) Long subjectId,
                                                           @RequestParam(required = false) Long topicId,
                                                           @RequestParam(required = false) Long skillId,
                                                           @RequestParam(required = false) ContentStatus status,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(defaultValue = "practiceExerciseId") String sortBy) {
        return new PagedModel<>(practiceCatalogService.findAll(subjectId, topicId, skillId, status, page, size, sortBy)
                .map(PracticeExerciseResponseDTO::from));
    }

    @GetMapping("/{practiceExerciseId}")
    public PracticeExerciseResponseDTO findById(@PathVariable long practiceExerciseId) {
        return PracticeExerciseResponseDTO.from(practiceCatalogService.findById(practiceExerciseId));
    }

    @PutMapping("/{practiceExerciseId}")
    public PracticeExerciseResponseDTO update(@PathVariable long practiceExerciseId,
                                              @RequestBody @Valid PracticeExerciseDTO body) {
        return PracticeExerciseResponseDTO.from(practiceCatalogService.findByIdAndUpdate(practiceExerciseId, body));
    }

    @PatchMapping("/{practiceExerciseId}/publish")
    public PracticeExerciseResponseDTO publish(@PathVariable long practiceExerciseId) {
        return PracticeExerciseResponseDTO.from(practiceCatalogService.publishPracticeExercise(practiceExerciseId));
    }

    @PatchMapping("/{practiceExerciseId}/withdraw")
    public PracticeExerciseResponseDTO withdraw(@PathVariable long practiceExerciseId) {
        return PracticeExerciseResponseDTO.from(practiceCatalogService.withdrawPracticeExercise(practiceExerciseId));
    }

    // 409 se la voce è già stata pubblicata o è consigliata da un test
    @DeleteMapping("/{practiceExerciseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long practiceExerciseId) {
        practiceCatalogService.deletePracticeExercise(practiceExerciseId);
    }
}
