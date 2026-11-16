package giuliacrepaldi.BStudent_Management.services.practice;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.practice.Topic;
import giuliacrepaldi.BStudent_Management.exceptions.ConflictException;
import giuliacrepaldi.BStudent_Management.exceptions.NotFoundException;
import giuliacrepaldi.BStudent_Management.payloads.practice.TopicDTO;
import giuliacrepaldi.BStudent_Management.payloads.practice.TopicUsagesDTO;
import giuliacrepaldi.BStudent_Management.repositories.practice.TopicsRepository;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ISubjectsService;
import giuliacrepaldi.BStudent_Management.services.practice.interfaces.ITopicsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TopicsService implements ITopicsService {

    private final TopicsRepository topicsRepository;
    private final ISubjectsService subjectsService;

    public TopicsService(TopicsRepository topicsRepository, ISubjectsService subjectsService) {
        this.topicsRepository = topicsRepository;
        this.subjectsService = subjectsService;
    }

    @Override
    @Transactional
    public Topic saveNewTopic(long subjectId, TopicDTO body) {
        Subject subject = subjectsService.findById(subjectId);
        String name = body.name().strip();
        if (topicsRepository.existsBySubject_SubjectIdAndTopicNameIgnoreCase(subjectId, name)) {
            throw new ConflictException("La materia ha già un argomento chiamato " + name);
        }
        return topicsRepository.save(Topic.builder()
                .subject(subject)
                .topicName(name)
                .imageReference(blankToNull(body.imageReference()))
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Topic findById(long topicId) {
        return topicsRepository.findById(topicId)
                .orElseThrow(() -> new NotFoundException(topicId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topic> findBySubject(long subjectId) {
        // 404 se la materia non esiste, invece di una lista vuota
        subjectsService.findById(subjectId);
        return topicsRepository.findBySubject_SubjectIdOrderByTopicName(subjectId);
    }

    @Override
    @Transactional
    public Topic findByIdAndUpdate(long topicId, TopicDTO body) {
        Topic topic = findById(topicId);
        String name = body.name().strip();
        if (topicsRepository.existsBySubject_SubjectIdAndTopicNameIgnoreCaseAndTopicIdNot(
                topic.getSubject().getSubjectId(), name, topicId)) {
            throw new ConflictException("La materia ha già un argomento chiamato " + name);
        }
        topic.setTopicName(name);
        topic.setImageReference(blankToNull(body.imageReference()));
        return topic;
    }

    @Override
    @Transactional(readOnly = true)
    public TopicUsagesDTO findUsages(long topicId) {
        findById(topicId);
        Object[] row = topicsRepository.countUsages(topicId).getFirst();
        return new TopicUsagesDTO(((Number) row[0]).longValue(), ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(), ((Number) row[3]).longValue());
    }

    @Override
    @Transactional
    public void deleteTopic(long topicId) {
        Topic topic = findById(topicId);
        TopicUsagesDTO usages = findUsages(topicId);
        if (usages.inUse()) {
            List<String> where = new ArrayList<>();
            if (usages.practiceExercises() > 0) where.add(usages.practiceExercises() + " esercizi della Palestra");
            if (usages.bankExercises() > 0) where.add(usages.bankExercises() + " esercizi del banco");
            if (usages.lessons() > 0) where.add(usages.lessons() + " lezioni");
            if (usages.resources() > 0) where.add(usages.resources() + " Risorse");
            throw new ConflictException("L'argomento è usato da " + String.join(", ", where)
                    + ": cambia prima il loro argomento");
        }
        topicsRepository.delete(topic);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
