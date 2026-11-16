package giuliacrepaldi.BStudent_Management.payloads.gamification;

// "Ripassa in Palestra": a tentativo chiuso, l'argomento di ogni aiuto usato e la Risorsa da rileggere.
// resourceSlug è null se l'argomento non ha una Risorsa pubblicata
public record HintReviewLinkDTO(String topicName, String subjectSlug, String resourceSlug, String resourceTitle) {
}
