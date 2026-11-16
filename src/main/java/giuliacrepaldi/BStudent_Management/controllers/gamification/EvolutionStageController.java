package giuliacrepaldi.BStudent_Management.controllers.gamification;

import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionStageRuleDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.EvolutionStageRuleResponseDTO;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IStudentXpService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**. Regole degli stadi, uguali per tutti i personaggi
@RestController
@RequestMapping("/admin/academy/evolution-stages")
public class EvolutionStageController {

    private final IStudentXpService studentXpService;

    public EvolutionStageController(IStudentXpService studentXpService) {
        this.studentXpService = studentXpService;
    }

    @GetMapping
    public List<EvolutionStageRuleResponseDTO> findAll() {
        return studentXpService.findRules().stream().map(EvolutionStageRuleResponseDTO::from).toList();
    }

    // Body: { "minXp": 800, "minAverageLevel": 3 }. Solo per gli stadi con soglie (dal II in poi)
    @PutMapping("/{stageNumber}")
    public EvolutionStageRuleResponseDTO update(@PathVariable int stageNumber,
                                                @RequestBody @Valid EvolutionStageRuleDTO body) {
        return EvolutionStageRuleResponseDTO.from(studentXpService.updateRule(stageNumber, body));
    }
}
