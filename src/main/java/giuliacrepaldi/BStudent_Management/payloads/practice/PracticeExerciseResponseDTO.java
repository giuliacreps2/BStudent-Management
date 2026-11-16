package giuliacrepaldi.BStudent_Management.payloads.practice;

import giuliacrepaldi.BStudent_Management.entities.exercises.QuestionBankEntry;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

// Vista dell'Admin. bankExerciseWithdrawn = l'esercizio è ritirato nel banco: la voce non si vede in Palestra
public record PracticeExerciseResponseDTO(
        Long practiceExerciseId,
        Long questionBankEntryId,
        String title,
        String internalTitle,
        boolean bankExerciseWithdrawn,
        Long subjectId,
        String subjectName,
        Long topicId,
        String topicName,
        Long skillId,
        String skillName,
        BigDecimal maxPoints,
        Integer estimatedMinutes,
        AccessTier accessTier,
        ContentStatus status,
        OffsetDateTime publishedAt,
        OffsetDateTime withdrawnAt) {

    // Legge esercizio, argomento, materia e skill LAZY: funziona nel controller (open-in-view attivo)
    public static PracticeExerciseResponseDTO from(PracticeExercise exercise) {
        QuestionBankEntry entry = exercise.getQuestionBankEntry();
        Topic topic = exercise.getTopic();
        return new PracticeExerciseResponseDTO(
                exercise.getPracticeExerciseId(),
                entry.getQuestionBankEntryId(),
                entry.getTitle(),
                entry.getInternalQuestionTitle(),
                entry.getStatus() == ContentStatus.withdrawn,
                topic.getSubject().getSubjectId(),
                topic.getSubject().getSubjectName(),
                topic.getTopicId(),
                topic.getTopicName(),
                exercise.getSkill().getSkillId(),
                exercise.getSkill().getSkillName(),
                exercise.getMaxPoints(),
                exercise.getEstimatedMinutes() == null ? null : exercise.getEstimatedMinutes().intValue(),
                exercise.getAccessTier(),
                exercise.getStatus(),
                exercise.getPublishedAt(),
                exercise.getWithdrawnAt());
    }
}
