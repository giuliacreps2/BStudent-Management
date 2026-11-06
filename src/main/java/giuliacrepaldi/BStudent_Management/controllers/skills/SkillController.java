package giuliacrepaldi.BStudent_Management.controllers.skills;

import giuliacrepaldi.BStudent_Management.payloads.skills.SkillDTO;
import giuliacrepaldi.BStudent_Management.payloads.skills.SkillResponseDTO;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISkillsService;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**
@RestController
@RequestMapping("/admin/academy/skills")
public class SkillController {

    private final ISkillsService skillsService;

    public SkillController(ISkillsService skillsService) {
        this.skillsService = skillsService;
    }

    // Body: { "code": "grammar", "name": "Grammatica", "description": "..." }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponseDTO create(@RequestBody @Valid SkillDTO body) {
        return SkillResponseDTO.from(skillsService.saveNewSkill(body));
    }

    @GetMapping
    public PagedModel<SkillResponseDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(defaultValue = "skillName") String sortBy) {
        return new PagedModel<>(skillsService.findAll(page, size, sortBy).map(SkillResponseDTO::from));
    }

    @GetMapping("/{skillId}")
    public SkillResponseDTO findById(@PathVariable long skillId) {
        return SkillResponseDTO.from(skillsService.findById(skillId));
    }

    // Nome e descrizione; il code nel body deve restare quello attuale
    @PutMapping("/{skillId}")
    public SkillResponseDTO update(@PathVariable long skillId, @RequestBody @Valid SkillDTO body) {
        return SkillResponseDTO.from(skillsService.findByIdAndUpdate(skillId, body));
    }

    // 409 se la skill è usata da pesi, materie, Palestra, accessori o progressi
    @DeleteMapping("/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long skillId) {
        skillsService.deleteSkill(skillId);
    }
}
