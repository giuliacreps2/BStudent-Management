package giuliacrepaldi.BStudent_Management.payloads.orientation;

import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

// Test di livello: solo il livello. Test di orientamento: solo gli esercizi consigliati (anche nessuno)
public record OrientationOptionDTO(
        @NotBlank @Size(max = 300) String text,
        CourseLevel level,
        // Sostituiti per intero a ogni salvataggio; null = nessuno
        @Size(max = 20) List<@Valid @NotNull Recommendation> recommendations) {

    // priority: a parità di risposte che lo consigliano vince il numero più basso (blueprint §8.1)
    public record Recommendation(@NotNull Long practiceExerciseId, @NotNull @Positive Integer priority) {
    }
}
