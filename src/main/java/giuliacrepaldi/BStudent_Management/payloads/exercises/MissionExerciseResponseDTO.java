package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;

import java.math.BigDecimal;

public record MissionExerciseResponseDTO(
        Long missionExerciseId,
        Long missionId,
        Long lessonId,
        Long questionBankEntryId,
        long allowedDurationMs,
        BigDecimal maxPoints) {

    public static MissionExerciseResponseDTO from(MissionExercise exercise) {
        return new MissionExerciseResponseDTO(
                exercise.getMissionExerciseId(),
                exercise.getMission().getMissionId(),
                exercise.getLesson().getLessonId(),
                exercise.getQuestionBankEntry().getQuestionBankEntryId(),
                exercise.getAllowedDurationMs(),
                exercise.getMaxPoints());
    }
}
