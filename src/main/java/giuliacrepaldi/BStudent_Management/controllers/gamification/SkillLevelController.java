package giuliacrepaldi.BStudent_Management.controllers.gamification;

import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.SkillLevelsDTO;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.ISkillLevelsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**. Punti necessari per ogni livello di una skill
@RestController
@RequestMapping("/admin/academy/skills/{skillId}/levels")
public class SkillLevelController {

    private final ISkillLevelsService skillLevelsService;

    public SkillLevelController(ISkillLevelsService skillLevelsService) {
        this.skillLevelsService = skillLevelsService;
    }

    @GetMapping
    public List<SkillLevelResponseDTO> findAll(@PathVariable long skillId) {
        return skillLevelsService.findLevels(skillId).stream().map(SkillLevelResponseDTO::from).toList();
    }

    // Body: { "minPoints": [0, 20, 50, 95, 165, 265], "applyToAllSkills": false }
    // 409 se toglie livelli o alza una soglia: gli studenti non scendono mai di livello
    @PutMapping
    public List<SkillLevelResponseDTO> replace(@PathVariable long skillId, @RequestBody @Valid SkillLevelsDTO body) {
        return skillLevelsService.replaceLevels(skillId, body).stream().map(SkillLevelResponseDTO::from).toList();
    }
}
