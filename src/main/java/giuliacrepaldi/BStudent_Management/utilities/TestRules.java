package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Calcolo dei risultati dei test dello studente, senza DB (blueprint §8). TestRulesTest li protegge
public final class TestRules {

    private TestRules() {
    }

    // Test di livello, regola prudente (§8.2): il livello più basso scelto almeno due volte;
    // se nessuno si ripete, il più basso tra quelli scelti
    public static CourseLevel levelFor(List<CourseLevel> chosen) {
        Map<CourseLevel, Integer> counts = new EnumMap<>(CourseLevel.class);
        chosen.forEach(level -> counts.merge(level, 1, Integer::sum));
        CourseLevel lowest = null;
        for (CourseLevel level : CourseLevel.values()) {
            if (!counts.containsKey(level)) {
                continue;
            }
            if (counts.get(level) >= 2) {
                return level;
            }
            if (lowest == null) {
                lowest = level;
            }
        }
        return lowest;
    }

    // Livello di partenza delle skill dal test di livello (Blueprint Gamification §2.6): partenza prudente,
    // Principiante 1, Intermedio 2, Avanzato 3
    public static int startingLevelFor(CourseLevel level) {
        return switch (level) {
            case beginner -> 1;
            case intermediate -> 2;
            case advanced -> 3;
        };
    }

    // Un esercizio consigliato da una risposta scelta, con la sua priorità (più bassa = prima)
    public record Recommendation(long practiceExerciseId, int priority) {
    }

    // Test di orientamento (§8.1): prima le voci consigliate da più risposte, poi priorità più bassa, poi ID.
    // Ogni elenco è quello di una risposta scelta; al massimo max voci
    public static List<Long> recommend(List<List<Recommendation>> byAnswer, int max) {
        Map<Long, Integer> counts = new HashMap<>();
        Map<Long, Integer> bestPriority = new HashMap<>();
        for (List<Recommendation> answer : byAnswer) {
            for (Recommendation recommendation : answer) {
                counts.merge(recommendation.practiceExerciseId(), 1, Integer::sum);
                bestPriority.merge(recommendation.practiceExerciseId(), recommendation.priority(), Math::min);
            }
        }
        List<Long> ids = new ArrayList<>(counts.keySet());
        ids.sort(Comparator.comparing((Long id) -> -counts.get(id))
                .thenComparing(bestPriority::get)
                .thenComparing(Comparator.naturalOrder()));
        return ids.subList(0, Math.min(max, ids.size()));
    }
}
