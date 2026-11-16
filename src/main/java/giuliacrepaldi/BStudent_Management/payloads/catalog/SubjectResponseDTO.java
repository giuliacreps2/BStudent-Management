package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;

import java.time.OffsetDateTime;

public record SubjectResponseDTO(
        Long subjectId,
        String name,
        String slug,
        String tagline,
        String imageReference,
        SubjectStatus status,
        OffsetDateTime publishedAt,
        String guideName,
        String guideImageReference,
        Long defaultSkillId) {

    public static SubjectResponseDTO from(Subject subject) {
        // Leggere l'id di un proxy LAZY non lo inizializza
        Long defaultSkillId = subject.getDefaultSkill() == null ? null : subject.getDefaultSkill().getSkillId();
        return new SubjectResponseDTO(
                subject.getSubjectId(),
                subject.getSubjectName(),
                subject.getSlug(),
                subject.getTagline(),
                subject.getImageReference(),
                subject.getStatus(),
                subject.getPublishedAt(),
                subject.getGuideName(),
                subject.getGuideImageReference(),
                defaultSkillId);
    }
}
