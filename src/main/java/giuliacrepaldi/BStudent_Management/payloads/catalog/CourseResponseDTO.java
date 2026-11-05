package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.time.OffsetDateTime;

// Vista dell'Admin: include stato e date editoriali
public record CourseResponseDTO(
        Long courseId,
        Long subjectId,
        String title,
        String shortDescription,
        String extendedDescription,
        String coverReference,
        AccessTier accessTier,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt) {

    public static CourseResponseDTO from(Course course) {
        return new CourseResponseDTO(
                course.getCourseId(),
                course.getSubject().getSubjectId(),
                course.getCourseTitle(),
                course.getCourseShortDescription(),
                course.getCourseDescription(),
                course.getCourseImage(),
                course.getAccessTier(),
                course.getStatus(),
                course.getPublishedAt(),
                course.getWithdrawnAt());
    }
}
