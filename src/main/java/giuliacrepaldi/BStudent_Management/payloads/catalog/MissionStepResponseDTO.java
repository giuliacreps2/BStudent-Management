package giuliacrepaldi.BStudent_Management.payloads.catalog;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

// Vista dell'Admin della sequenza: kind "lesson" valorizza lessonId, kind "exercise" i campi dell'esercizio.
// status: della lezione, oppure dell'esercizio nella missione (draft = nascosto).
// bankStatus: solo per gli esercizi, stato nel banco esercizi (withdrawn = nascosto ovunque)
public record MissionStepResponseDTO(
        Long stepId,
        int sortOrder,
        String kind,
        String title,
        ContentStatus status,
        ContentStatus bankStatus,
        Long lessonId,
        Long missionExerciseId,
        Long questionBankEntryId) {

    public static MissionStepResponseDTO from(MissionStep step) {
        if (step.isExercise()) {
            MissionExercise exercise = step.getMissionExercise();
            return new MissionStepResponseDTO(step.getMissionStepId(), step.getStepSortOrder(), "exercise",
                    exercise.getQuestionBankEntry().getInternalQuestionTitle(),
                    exercise.getStatus(), exercise.getQuestionBankEntry().getStatus(),
                    null, exercise.getMissionExerciseId(), exercise.getQuestionBankEntry().getQuestionBankEntryId());
        }
        Lesson lesson = step.getLesson();
        return new MissionStepResponseDTO(step.getMissionStepId(), step.getStepSortOrder(), "lesson",
                lesson.getLessonTitle(), lesson.getStatus(), null, lesson.getLessonId(), null, null);
    }
}
