package giuliacrepaldi.BStudent_Management.controllers.skills;

import giuliacrepaldi.BStudent_Management.payloads.catalog.ReorderDTO;
import giuliacrepaldi.BStudent_Management.payloads.skills.SubjectSkillDTO;
import giuliacrepaldi.BStudent_Management.payloads.skills.SubjectSkillResponseDTO;
import giuliacrepaldi.BStudent_Management.services.skills.interfaces.ISubjectSkillsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**. Ogni operazione restituisce l'elenco aggiornato
@RestController
@RequestMapping("/admin/academy/subjects/{subjectId}/skills")
public class SubjectSkillController {

    private final ISubjectSkillsService subjectSkillsService;

    public SubjectSkillController(ISubjectSkillsService subjectSkillsService) {
        this.subjectSkillsService = subjectSkillsService;
    }

    @GetMapping
    public List<SubjectSkillResponseDTO> findAll(@PathVariable long subjectId) {
        return subjectSkillsService.findBySubject(subjectId).stream().map(SubjectSkillResponseDTO::from).toList();
    }

    // Body: { "skillId": 3 }
    @PostMapping
    public List<SubjectSkillResponseDTO> add(@PathVariable long subjectId, @RequestBody @Valid SubjectSkillDTO body) {
        return subjectSkillsService.addSkill(subjectId, body.skillId()).stream().map(SubjectSkillResponseDTO::from).toList();
    }

    // Body: { "ids": [skillId, ...] } con tutti e soli gli ID delle skill della materia
    @PutMapping("/order")
    public List<SubjectSkillResponseDTO> reorder(@PathVariable long subjectId, @RequestBody @Valid ReorderDTO body) {
        return subjectSkillsService.reorderSkills(subjectId, body.ids()).stream().map(SubjectSkillResponseDTO::from).toList();
    }

    // 409 se è la skill predefinita o se è usata da esercizi della materia
    @DeleteMapping("/{skillId}")
    public List<SubjectSkillResponseDTO> remove(@PathVariable long subjectId, @PathVariable long skillId) {
        return subjectSkillsService.removeSkill(subjectId, skillId).stream().map(SubjectSkillResponseDTO::from).toList();
    }
}
