package giuliacrepaldi.BStudent_Management.payloads.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.enums.skills.StatusSkill;

public record SkillResponseDTO(Long skillId, String code, String name, String description, StatusSkill status) {

    public static SkillResponseDTO from(Skill skill) {
        return new SkillResponseDTO(skill.getSkillId(), skill.getCodeSkill(), skill.getSkillName(),
                skill.getSkillDescription(), skill.getStatusSkill());
    }
}
