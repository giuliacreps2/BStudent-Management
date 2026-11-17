package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;

// Skill come la mostra il frontend: icona e colore da un elenco fisso, aspetto neutro per i codici nuovi (§7.3)
public record SkillRefDTO(Long skillId, String code, String name, String description, String icon, String color) {

    public static SkillRefDTO from(Skill skill) {
        return new SkillRefDTO(skill.getSkillId(), skill.getCodeSkill(), skill.getSkillName(), skill.getSkillDescription(),
                skill.getIcon(), skill.getColor());
    }
}
