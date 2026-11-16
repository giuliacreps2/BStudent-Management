package giuliacrepaldi.BStudent_Management.payloads.practice;

// Dove è usato l'argomento: il frontend lo mostra prima di eliminarlo; tutto a zero = si può eliminare
public record TopicUsagesDTO(long practiceExercises, long bankExercises, long lessons, long resources) {

    public boolean inUse() {
        return practiceExercises + bankExercises + lessons + resources > 0;
    }
}
