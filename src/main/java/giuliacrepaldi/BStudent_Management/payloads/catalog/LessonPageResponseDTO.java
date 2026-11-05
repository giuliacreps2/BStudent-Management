package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.LessonPage;
import giuliacrepaldi.BStudent_Management.enums.catalog.Kind;

public record LessonPageResponseDTO(
        Long lessonPageId,
        Long lessonId,
        int sortOrder,
        Kind kind,
        String formattedText,
        String imageReference,
        String videoProvider,
        String videoIdentifier) {

    public static LessonPageResponseDTO from(LessonPage page) {
        return new LessonPageResponseDTO(
                page.getLessonPageId(),
                page.getLesson().getLessonId(),
                page.getLessonPageSortOrder(),
                page.getKind(),
                page.getFormattedText(),
                page.getImageReference(),
                page.getVideoProvider(),
                page.getVideoIdentifier());
    }
}
