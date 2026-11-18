package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseAttemptStatus;
import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.exercises.RubricCategory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// Una consegna da correggere (Blueprint Svolgimento §4.1): l'esercizio nella versione svolta dallo studente,
// la sua risposta, le soluzioni e la spiegazione dell'autrice, le rubriche come guida (nessun punteggio per
// criterio). missionAttemptNumber solo per le missioni. assessment c'è quando la consegna è già corretta
public record SubmissionDetailDTO(long exerciseAttemptId, ExerciseContext context, ExerciseAttemptStatus status,
                                  String studentUsername, String subjectName, String place, Integer missionAttemptNumber,
                                  String exerciseTitle, ExerciseViewDTO exercise, String answer,
                                  List<String> referenceAnswers, String generalExplanation,
                                  List<RubricCriterion> rubric, BigDecimal maxPoints, BigDecimal passingThreshold,
                                  OffsetDateTime submittedAt, Assessment assessment) {

    public record RubricCriterion(RubricCategory category, String description) {
    }

    public record Assessment(BigDecimal percentage, boolean passed, String feedback, OffsetDateTime assessedAt) {
    }
}
