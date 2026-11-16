package giuliacrepaldi.BStudent_Management.payloads.gamification;

import java.util.List;

// Personaggio in uso: lo stadio è dello studente, uguale per tutti i personaggi (Blueprint Gamification §4.6).
// imageReference = immagine dello stadio attuale; motto facoltativo
public record CharacterDTO(Long characterId, String code, String name, String description, String motto,
                           int stageNumber, String stageName, String imageReference,
                           List<CharacterStageResponseDTO> stages) {
}
