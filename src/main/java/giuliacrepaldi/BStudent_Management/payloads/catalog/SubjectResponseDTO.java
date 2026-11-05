package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;

public record SubjectResponseDTO(Long subjectId, String name, Long defaultSkillId) {

    public static SubjectResponseDTO from(Subject subject) {
        // Leggere l'id di un proxy LAZY non lo inizializza
        Long defaultSkillId = subject.getDefaultSkill() == null ? null : subject.getDefaultSkill().getSkillId();
        return new SubjectResponseDTO(subject.getSubjectId(), subject.getSubjectName(), defaultSkillId);
    }
}
