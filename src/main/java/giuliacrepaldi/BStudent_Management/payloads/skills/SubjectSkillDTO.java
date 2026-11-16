package giuliacrepaldi.BStudent_Management.payloads.skills;

import jakarta.validation.constraints.NotNull;

// Skill del catalogo da aggiungere alla materia (in coda all'ordine)
public record SubjectSkillDTO(@NotNull Long skillId) {
}
