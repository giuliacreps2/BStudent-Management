package giuliacrepaldi.BStudent_Management.controllers.catalog;

import giuliacrepaldi.BStudent_Management.payloads.catalog.SubjectDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.SubjectResponseDTO;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ISubjectsService;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**
@RestController
@RequestMapping("/admin/academy/subjects")
public class SubjectController {

    private final ISubjectsService subjectsService;

    public SubjectController(ISubjectsService subjectsService) {
        this.subjectsService = subjectsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectResponseDTO create(@RequestBody @Valid SubjectDTO body) {
        return SubjectResponseDTO.from(subjectsService.saveNewSubject(body));
    }

    // PagedModel: forma JSON stabile della pagina (content + page), al posto del PageImpl grezzo
    @GetMapping
    public PagedModel<SubjectResponseDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(defaultValue = "subjectName") String sortBy) {
        return new PagedModel<>(subjectsService.findAll(page, size, sortBy).map(SubjectResponseDTO::from));
    }

    @GetMapping("/{subjectId}")
    public SubjectResponseDTO findById(@PathVariable long subjectId) {
        return SubjectResponseDTO.from(subjectsService.findById(subjectId));
    }

    @PutMapping("/{subjectId}")
    public SubjectResponseDTO update(@PathVariable long subjectId, @RequestBody @Valid SubjectDTO body) {
        return SubjectResponseDTO.from(subjectsService.findByIdAndUpdate(subjectId, body));
    }

    // 409 se la materia ha dei corsi
    @DeleteMapping("/{subjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long subjectId) {
        subjectsService.deleteSubject(subjectId);
    }
}
