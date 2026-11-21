package giuliacrepaldi.BStudent_Management.payloads.progression;

import giuliacrepaldi.BStudent_Management.enums.progression.StudyEventKind;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

// Un evento del registro di studio (blueprint §10.5), inviato dal frontend a tratti.
// lesson_read: lessonId (+ lessonPageId facoltativo), startedAt e activeMs (tempo attivo del tratto: segnale ogni
//   30 s mentre la pagina è visibile, stop dopo 10 minuti senza interazione o con la scheda nascosta).
// resource_read: resourceId, startedAt e activeMs. recommendation_open: practiceExerciseId (senza durata)
public record StudyEventDTO(@NotNull StudyEventKind kind, Long lessonId, Long lessonPageId, Long resourceId,
                            Long practiceExerciseId, OffsetDateTime startedAt, @Positive Long activeMs) {
}
