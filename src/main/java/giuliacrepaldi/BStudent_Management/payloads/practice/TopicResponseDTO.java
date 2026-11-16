package giuliacrepaldi.BStudent_Management.payloads.practice;

import giuliacrepaldi.BStudent_Management.entities.practice.Topic;

public record TopicResponseDTO(Long topicId, Long subjectId, String name, String imageReference,
                               Integer referenceYear) {

    public static TopicResponseDTO from(Topic topic) {
        // Leggere l'id di un proxy LAZY non lo inizializza
        return new TopicResponseDTO(topic.getTopicId(), topic.getSubject().getSubjectId(), topic.getTopicName(),
                topic.getImageReference(), topic.getReferenceYear() == null ? null : topic.getReferenceYear().intValue());
    }
}
