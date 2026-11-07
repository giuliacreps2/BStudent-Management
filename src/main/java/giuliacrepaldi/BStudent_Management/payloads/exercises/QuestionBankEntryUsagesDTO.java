package giuliacrepaldi.BStudent_Management.payloads.exercises;

import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;

import java.util.List;

// Dove è usato l'esercizio: il frontend lo mostra prima del ritiro e avvisa per le missioni
// che resterebbero senza esercizi (leftWithoutExercises)
public record QuestionBankEntryUsagesDTO(List<MissionUsage> missions, boolean publishedInPractice) {

    public record MissionUsage(
            Long missionId,
            String missionTitle,
            ContentStatus missionStatus,
            Long courseId,
            String courseTitle,
            boolean leftWithoutExercises) {
    }
}
