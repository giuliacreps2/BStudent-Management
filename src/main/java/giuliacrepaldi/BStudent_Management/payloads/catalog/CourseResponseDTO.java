package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;

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
        String slug,
        String tagline,
        CourseLevel level,
        OffsetDateTime featuredUntil,
        String worldTheme,
        String quote,
        String quoteAuthor,
        boolean entryCourse,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static CourseResponseDTO from(Course course) {
        return new CourseResponseDTO(
                course.getCourseId(),
                course.getSubject().getSubjectId(),
                course.getCourseTitle(),
                course.getCourseShortDescription(),
                course.getCourseDescription(),
                course.getCourseImage(),
                course.getAccessTier(),
                course.getSlug(),
                course.getTagline(),
                course.getLevel(),
                course.getFeaturedUntil(),
                course.getWorldTheme(),
                course.getQuote(),
                course.getQuoteAuthor(),
                course.isEntryCourse(),
                course.getStatus(),
                course.getPublishedAt(),
                course.getWithdrawnAt(),
                course.getCreatedAt(),
                course.getUpdatedAt());
    }
}
