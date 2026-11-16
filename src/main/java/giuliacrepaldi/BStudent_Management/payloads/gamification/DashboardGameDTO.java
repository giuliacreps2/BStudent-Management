package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.util.List;

// Dati di gioco della dashboard: personaggio, cammino verso la prossima evoluzione, livelli delle skill della
// materia scelta, Serie. character è null finché non esistono personaggi pubblicati
public record DashboardGameDTO(CharacterDTO character, EvolutionProgressDTO evolution,
                               List<SkillLevelStateDTO> skillLevels, StreakDTO streak) {
}
