package giuliacrepaldi.BStudent_Management.services.practice.interfaces;

/**
 * Argomenti della materia (blueprint §7.1, §7.3, §10.5).
 * Nome unico nella materia; immagine facoltativa (v0.9), usata nella Palestra al posto di quella della skill.
 * L'argomento collega esercizio -> teoria: lo usano voci della Palestra, esercizi del banco, lezioni e Risorse.
 *
 * <h2>TODO — metodi da implementare (Fase 1, Admin)</h2>
 * Nell'interfaccia restano come commento finché non esistono DTO e implementazione.
 * <pre>
 * Topic saveNewTopic(long subjectId, TopicDTO body);
 *     // nome unico nella materia (ignorando maiuscole) -> ConflictException con messaggio chiaro
 * Topic findById(long topicId);
 * List&lt;Topic&gt; findBySubject(long subjectId);
 *     // ordinati per nome; pochi per materia: niente paginazione
 * Topic findByIdAndUpdate(long topicId, TopicDTO body);
 *     // nome e immagine; la materia non cambia (sposterebbe esercizi e lezioni in un'altra materia)
 * TopicUsagesDTO findUsages(long topicId);
 *     // dove è usato: voci Palestra, esercizi del banco, lezioni, Risorse, test (come QuestionBankEntryUsagesDTO)
 * void deleteTopic(long topicId);
 *     // solo se non è usato da nulla, altrimenti ConflictException che dice dove è usato
 * </pre>
 * DTO da creare: TopicDTO (name, imageReference), TopicResponseDTO, TopicUsagesDTO.
 * Controller: /admin/academy/subjects/{subjectId}/topics e /admin/academy/topics/{topicId}.
 */
public interface ITopicsService {
}
