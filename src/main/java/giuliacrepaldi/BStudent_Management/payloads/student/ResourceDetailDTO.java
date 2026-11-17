package giuliacrepaldi.BStudent_Management.payloads.student;

import tools.jackson.databind.JsonNode;

// Pagina di una Risorsa (blueprint §7.4). content = sezioni della pagina (formato a blocchi), null se la Risorsa
// è premium e lo studente non ha accesso: in quel caso restano titolo, descrizione e "In breve".
// saved = segnalibro nel Quaderno. hasExercises = mostrare "Vai agli esercizi" (voci visibili dell'argomento).
// "Approfondisci" non è restituito nell'MVP
public record ResourceDetailDTO(Long resourceId, ResourceCardDTO resource, String summaryText, JsonNode content,
                                Long topicId, String topicName, boolean hasExercises, boolean saved) {
}
