package giuliacrepaldi.BStudent_Management.utilities;

import giuliacrepaldi.BStudent_Management.payloads.attempts.AnswerDTO;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Forma dei due campi JSON del tentativo, in un punto solo:
// - response: { "selectedAnswerId": 12 } oppure { "text": "..." };
// - configuration_snapshot: i valori usati dal tentativo, che restano anche se l'autrice li cambia dopo
//   (blueprint §6.1): punti massimi, voce della Palestra o esercizio della missione, ordine delle opzioni
public final class AttemptJson {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private AttemptJson() {
    }

    public static String writeAnswer(AnswerDTO answer) {
        ObjectNode node = MAPPER.createObjectNode();
        if (answer != null && answer.selectedAnswerId() != null) {
            node.put("selectedAnswerId", answer.selectedAnswerId());
        }
        if (answer != null && answer.text() != null) {
            node.put("text", answer.text());
        }
        return MAPPER.writeValueAsString(node);
    }

    public static AnswerDTO readAnswer(String response) {
        if (response == null || response.isBlank()) {
            return AnswerDTO.empty();
        }
        JsonNode node = MAPPER.readTree(response);
        return new AnswerDTO(node.hasNonNull("selectedAnswerId") ? node.get("selectedAnswerId").asLong() : null,
                node.hasNonNull("text") ? node.get("text").asString() : null);
    }

    // sourceField: "practiceExerciseId" oppure "missionExerciseId"
    public static String writeConfiguration(String sourceField, long sourceId, BigDecimal maxPoints, List<Long> answerOrder) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put(sourceField, sourceId);
        node.put("maxPoints", maxPoints);
        ArrayNode order = node.putArray("answerOrder");
        answerOrder.forEach(order::add);
        return MAPPER.writeValueAsString(node);
    }

    public static List<Long> answerOrder(String configuration) {
        List<Long> order = new ArrayList<>();
        JsonNode node = MAPPER.readTree(configuration).get("answerOrder");
        if (node != null && node.isArray()) {
            node.forEach(id -> order.add(id.asLong()));
        }
        return order;
    }

    public static BigDecimal maxPoints(String configuration) {
        JsonNode node = MAPPER.readTree(configuration).get("maxPoints");
        return node == null || node.isNull() ? null : node.decimalValue();
    }
}
