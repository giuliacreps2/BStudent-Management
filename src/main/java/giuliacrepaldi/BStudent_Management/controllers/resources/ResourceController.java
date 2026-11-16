package giuliacrepaldi.BStudent_Management.controllers.resources;

import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.resources.ResourceCategory;
import giuliacrepaldi.BStudent_Management.payloads.resources.ResourceDTO;
import giuliacrepaldi.BStudent_Management.payloads.resources.ResourceResponseDTO;
import giuliacrepaldi.BStudent_Management.services.resources.interfaces.IResourcesService;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**
@RestController
@RequestMapping("/admin/academy")
public class ResourceController {

    private final IResourcesService resourcesService;

    public ResourceController(IResourcesService resourcesService) {
        this.resourcesService = resourcesService;
    }

    @PostMapping("/subjects/{subjectId}/resources")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceResponseDTO create(@PathVariable long subjectId, @RequestBody @Valid ResourceDTO body) {
        return ResourceResponseDTO.from(resourcesService.saveNewResource(subjectId, body));
    }

    // Filtri facoltativi: materia, categoria, stato
    @GetMapping("/resources")
    public PagedModel<ResourceResponseDTO> findAll(@RequestParam(required = false) Long subjectId,
                                                   @RequestParam(required = false) ResourceCategory category,
                                                   @RequestParam(required = false) ContentStatus status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(defaultValue = "title") String sortBy) {
        return new PagedModel<>(resourcesService.findAll(subjectId, category, status, page, size, sortBy)
                .map(ResourceResponseDTO::from));
    }

    @GetMapping("/resources/{resourceId}")
    public ResourceResponseDTO findById(@PathVariable long resourceId) {
        return ResourceResponseDTO.from(resourcesService.findById(resourceId));
    }

    @PutMapping("/resources/{resourceId}")
    public ResourceResponseDTO update(@PathVariable long resourceId, @RequestBody @Valid ResourceDTO body) {
        return ResourceResponseDTO.from(resourcesService.findByIdAndUpdate(resourceId, body));
    }

    @PatchMapping("/resources/{resourceId}/publish")
    public ResourceResponseDTO publish(@PathVariable long resourceId) {
        return ResourceResponseDTO.from(resourcesService.publishResource(resourceId));
    }

    @PatchMapping("/resources/{resourceId}/withdraw")
    public ResourceResponseDTO withdraw(@PathVariable long resourceId) {
        return ResourceResponseDTO.from(resourcesService.withdrawResource(resourceId));
    }

    // 409 se la risorsa è già stata pubblicata
    @DeleteMapping("/resources/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long resourceId) {
        resourcesService.deleteResource(resourceId);
    }
}
