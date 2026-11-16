package giuliacrepaldi.BStudent_Management.payloads.skills;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.entities.skills.SubjectSkill;
import giuliacrepaldi.BStudent_Management.enums.skills.StatusSkill;

// Skill della materia, nell'ordine dell'autrice; la skill va caricata (findBySubjectOrdered)
public record SubjectSkillResponseDTO(Long subjectId, Long skillId, String code, String name, String icon, String color,
                                      StatusSkill status, int sortOrder) {

    public static SubjectSkillResponseDTO from(SubjectSkill subjectSkill) {
        Skill skill = subjectSkill.getSkill();
        return new SubjectSkillResponseDTO(subjectSkill.getId().getSubjectId(), skill.getSkillId(), skill.getCodeSkill(),
                skill.getSkillName(), skill.getIcon(), skill.getColor(), skill.getStatusSkill(), subjectSkill.getSortOrder());
    }
}
