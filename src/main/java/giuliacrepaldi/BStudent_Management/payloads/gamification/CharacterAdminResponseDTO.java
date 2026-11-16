package giuliacrepaldi.BStudent_Management.payloads.gamification;

import giuliacrepaldi.BStudent_Management.entities.skills.Skin;
import giuliacrepaldi.BStudent_Management.entities.skills.SkinEvolution;
import giuliacrepaldi.BStudent_Management.enums.skills.StatusSkill;

import java.util.List;

// Vista dell'Admin: il personaggio con gli stadi già preparati. missingStages = stadi senza immagine,
// da completare prima di pubblicare
public record CharacterAdminResponseDTO(Long characterId, String code, String name, String description,
                                        String motto, boolean starter, StatusSkill status,
                                        List<CharacterStageResponseDTO> stages, List<Integer> missingStages) {

    public static CharacterAdminResponseDTO from(Skin skin, List<SkinEvolution> evolutions, List<Integer> missingStages) {
        return new CharacterAdminResponseDTO(skin.getSkinId(), skin.getSkinCode(), skin.getSkinName(),
                skin.getSkinDescription(), skin.getMotto(), skin.isStarter(), skin.getStatusSkin(),
                evolutions.stream().map(e -> new CharacterStageResponseDTO(e.getStageNumber(), e.getSkinName(),
                        e.getSkinDescription(), true, e.getSkinAssetPath())).toList(),
                missingStages);
    }
}
