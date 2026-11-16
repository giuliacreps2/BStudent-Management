package giuliacrepaldi.BStudent_Management.services.practice.interfaces;

import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.payloads.practice.TopicDTO;
import giuliacrepaldi.BStudent_Management.payloads.practice.TopicUsagesDTO;

import java.util.List;

/**
 * Argomenti della materia (blueprint §7.1, §7.3, §10.5).
 * Nome unico nella materia (ignorando le maiuscole); immagine facoltativa (v0.9), usata nella Palestra al
 * posto di quella della skill.
 * L'argomento collega esercizio -> teoria: lo usano voci della Palestra, esercizi del banco, lezioni e Risorse.
 * <p>
 * - La materia non cambia: sposterebbe esercizi e lezioni in un'altra materia.
 * - deleteTopic: solo se non è usato da nulla (findUsages), altrimenti ConflictException che dice dove.
 */
public interface ITopicsService {

    //Admin
    //CRUD -> create, read, update, delete

    Topic saveNewTopic(long subjectId, TopicDTO body);

    Topic findById(long topicId);

    List<Topic> findBySubject(long subjectId); // per nome; pochi per materia: niente paginazione

    Topic findByIdAndUpdate(long topicId, TopicDTO body); // nome e immagine

    TopicUsagesDTO findUsages(long topicId);

    void deleteTopic(long topicId);
}
