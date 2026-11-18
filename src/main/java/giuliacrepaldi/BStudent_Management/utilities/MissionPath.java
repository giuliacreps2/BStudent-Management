package giuliacrepaldi.BStudent_Management.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// A che punto è lo studente nella missione, senza DB (blueprint §9.1; Blueprint Svolgimento §6).
// Un passo è fatto se la lezione è stata letta nel percorso o se l'esercizio è un checkpoint (superato).
// Il passo attuale è il primo non fatto: così un esercizio ritirato, che non è più tra i passi visibili, viene
// saltato da solo, e una lezione aggiunta dall'autrice prima del punto raggiunto va letta. MissionPathTest li protegge
public final class MissionPath {

    public static final String DONE = "done";
    public static final String CURRENT = "current";
    public static final String TODO = "todo";

    private MissionPath() {
    }

    // id = lezione oppure esercizio della missione
    public record Step(boolean exercise, long id) {
    }

    // currentIndex = posizione del passo attuale; -1 se sono tutti fatti (missione completata)
    public record Position(List<String> statuses, int currentIndex) {

        public boolean complete() {
            return currentIndex < 0;
        }
    }

    public static Position of(List<Step> steps, Collection<Long> readLessonIds, Collection<Long> passedExerciseIds) {
        List<String> statuses = new ArrayList<>();
        int current = -1;
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            boolean done = step.exercise() ? passedExerciseIds.contains(step.id()) : readLessonIds.contains(step.id());
            if (done) {
                statuses.add(DONE);
            } else if (current < 0) {
                statuses.add(CURRENT);
                current = i;
            } else {
                statuses.add(TODO);
            }
        }
        return new Position(statuses, current);
    }
}
