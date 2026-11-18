package giuliacrepaldi.BStudent_Management.payloads.attempts;

import giuliacrepaldi.BStudent_Management.enums.attempts.ExerciseContext;
import giuliacrepaldi.BStudent_Management.enums.exercises.KindQuestion;

import java.time.OffsetDateTime;

// Una riga di "Consegne da correggere", dalla più vecchia.
// place: "Palestra" oppure "Missione «titolo» · corso"; exerciseTitle è il titolo interno del banco
public record SubmissionListItemDTO(long exerciseAttemptId, ExerciseContext context, String studentUsername,
                                    String subjectName, String place, String exerciseTitle, KindQuestion kind,
                                    OffsetDateTime submittedAt) {
}
