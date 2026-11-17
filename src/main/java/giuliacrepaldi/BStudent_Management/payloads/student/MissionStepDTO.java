package giuliacrepaldi.BStudent_Management.payloads.student;

// Tappa della missione nello stepper (blueprint §5.9): lezione o esercizio, con il titolo per lo studente.
// kind: lesson | exercise. status: done | current | todo; fino al Blocco 5 (checkpoint) la prima è current.
// Il traguardo finale "Ricompensa" lo aggiunge il frontend
public record MissionStepDTO(int order, String kind, String title, String status) {
}
