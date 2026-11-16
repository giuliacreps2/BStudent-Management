package giuliacrepaldi.BStudent_Management.services.gamification;

import giuliacrepaldi.BStudent_Management.entities.gamification.GamificationSetting;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.gamification.GamificationSettingsDTO;
import giuliacrepaldi.BStudent_Management.repositories.gamification.GamificationSettingsRepository;
import giuliacrepaldi.BStudent_Management.services.gamification.interfaces.IGamificationSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GamificationSettingsService implements IGamificationSettingsService {

    // Riga unica: il DB impone settings_id = 1
    private static final short SETTINGS_ID = 1;

    private final GamificationSettingsRepository settingsRepository;

    public GamificationSettingsService(GamificationSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public GamificationSetting get() {
        return settingsRepository.findById(SETTINGS_ID)
                .orElseThrow(() -> new NotFoundException("Impostazioni di gioco non trovate: manca la riga iniziale nel database"));
    }

    @Override
    @Transactional
    public GamificationSetting update(GamificationSettingsDTO body) {
        GamificationSetting settings = get();
        // Valgono da ora: vite, premi e ricariche già assegnati non cambiano
        settings.setBaseLives(body.baseLives());
        settings.setLifeRechargeHours(body.lifeRechargeHours());
        settings.setDailyChallengeTarget(body.dailyChallengeTarget());
        settings.setPracticeStreakForLife(body.practiceStreakForLife());
        settings.setPracticeFailsForPause(body.practiceFailsForPause());
        return settings;
    }
}
