package giuliacrepaldi.BStudent_Management.controllers.gamification;

import giuliacrepaldi.BStudent_Management.payloads.gamification.GamificationSettingsDTO;
import giuliacrepaldi.BStudent_Management.payloads.gamification.GamificationSettingsResponseDTO;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IGamificationSettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**. "Impostazioni di gioco" nell'Admin
@RestController
@RequestMapping("/admin/academy/game-settings")
public class GameSettingsController {

    private final IGamificationSettingsService settingsService;

    public GameSettingsController(IGamificationSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public GamificationSettingsResponseDTO get() {
        return GamificationSettingsResponseDTO.from(settingsService.get());
    }

    // Body: { "baseLives": 5, "lifeRechargeHours": 10, "dailyChallengeTarget": 3,
    //         "practiceStreakForLife": 3, "practiceFailsForPause": 3 }
    @PutMapping
    public GamificationSettingsResponseDTO update(@RequestBody @Valid GamificationSettingsDTO body) {
        return GamificationSettingsResponseDTO.from(settingsService.update(body));
    }
}
