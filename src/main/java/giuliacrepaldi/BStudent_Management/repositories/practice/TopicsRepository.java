package giuliacrepaldi.BStudent_Management.repositories.practice;

import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicsRepository extends JpaRepository<Topic, Long> {

    List<Topic> findBySubject_SubjectIdOrderByTopicName(Long subjectId);

    boolean existsBySubject_SubjectIdAndTopicNameIgnoreCase(Long subjectId, String topicName);

    boolean existsBySubject_SubjectIdAndTopicNameIgnoreCaseAndTopicIdNot(Long subjectId, String topicName, Long topicId);

    // Tutte le tabelle con una FK verso topics, nell'ordine di TopicUsagesDTO.
    // Da aggiornare se una nuova tabella punta a topics (es. collegamenti delle Risorse, che non hanno Admin nell'MVP)
    @Query(value = """
            SELECT (SELECT count(*) FROM practice_exercises WHERE topic_id = :id),
                   (SELECT count(*) FROM question_bank_entries WHERE topic_id = :id),
                   (SELECT count(*) FROM lessons WHERE topic_id = :id),
                   (SELECT count(*) FROM resources WHERE topic_id = :id)
                 + (SELECT count(*) FROM resource_links WHERE target_topic_id = :id)
            """, nativeQuery = true)
    List<Object[]> countUsages(@Param("id") Long topicId);
}
