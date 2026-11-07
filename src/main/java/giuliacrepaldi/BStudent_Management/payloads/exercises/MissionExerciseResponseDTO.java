package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.entities.exercises.MissionExercise;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.math.BigDecimal;

// status: draft = nascosto allo studente (in una missione pubblicata), published = visibile
public record MissionExerciseResponseDTO(
        Long missionExerciseId,
        Long missionId,
        Long questionBankEntryId,
        ContentStatus status,
        long allowedDurationMs,
        BigDecimal maxPoints) {

    public static MissionExerciseResponseDTO from(MissionExercise exercise) {
        return new MissionExerciseResponseDTO(
                exercise.getMissionExerciseId(),
                exercise.getMission().getMissionId(),
                exercise.getQuestionBankEntry().getQuestionBankEntryId(),
                exercise.getStatus(),
                exercise.getAllowedDurationMs(),
                exercise.getMaxPoints());
    }
}
