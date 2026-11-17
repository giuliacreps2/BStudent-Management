package giuliacrepaldi.BStudent_Management.payloads.student;

import java.util.List;

// Voci di un argomento. referenceYear = classe di riferimento (anno di scuola superiore, null = per tutti):
// serve per le sezioni "Per la tua classe" e "Più avanti nel programma" (Blueprint Gamification §3-bis)
public record PracticeTopicGroupDTO(Long topicId, String topicName, String imageReference, Integer referenceYear,
                                    List<PracticeItemDTO> items) {
}
