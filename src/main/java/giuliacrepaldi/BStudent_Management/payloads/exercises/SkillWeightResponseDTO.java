package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExerciseSkill;

import java.math.BigDecimal;

// Richiede la skill già caricata (MissionExerciseSkillsRepository.findWithSkill)
public record SkillWeightResponseDTO(Long skillId, String skillCode, String skillName, BigDecimal weight) {

    public static SkillWeightResponseDTO from(MissionExerciseSkill weight) {
        return new SkillWeightResponseDTO(
                weight.getSkill().getSkillId(),
                weight.getSkill().getCodeSkill(),
                weight.getSkill().getSkillName(),
                weight.getWeight());
    }
}
