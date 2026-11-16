package giuliacrepaldi.BStudent_Management.payloads.gamification;

import tools.jackson.databind.JsonNode;

// Aiuto del cappello (Blueprint Gamification §6.2): l'argomento dell'esercizio e l'"In breve" della Risorsa
// collegata (testo e mini esempio), se esiste. alreadyOpened = riaperto nello stesso tentativo, senza
// consumare cariche. Il collegamento alla Risorsa intera compare solo a fine tentativo
public record HintDTO(String topicName, String summaryText, JsonNode summaryExample, boolean alreadyOpened,
                      HintChargesDTO charges) {
}
