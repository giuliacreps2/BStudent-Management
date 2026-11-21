package giuliacrepaldi.BStudent_Management.payloads.progression;

import java.util.List;

// Tempo di studio (blueprint §10.5): tempo attivo su teoria (lezioni, Risorse) ed esercizi, negli ultimi giorni.
// Solo giorni con attività; minuti arrotondati. averageMinutesPerActiveDay = minuti per giorno di attività.
// Tono neutro e nessun obiettivo di tempo: è solo una vista
public record StudyTimeDTO(int days, int totalMinutes, int theoryMinutes, int exerciseMinutes, int activeDays,
                           int averageMinutesPerActiveDay, List<Day> byDay) {

    public record Day(String date, int theoryMinutes, int exerciseMinutes) {
    }
}
