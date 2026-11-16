package giuliacrepaldi.BStudent_Management.controllers.practice;

import giuliacrepaldi.BStudent_Management.payloads.practice.TopicDTO;
import giuliacrepaldi.BStudent_Management.payloads.practice.TopicResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.practice.TopicUsagesDTO;
import giuliacrepaldi.BStudent_Management.services.practice.interfaces.ITopicsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**
@RestController
@RequestMapping("/admin/academy")
public class TopicController {

    private final ITopicsService topicsService;

    public TopicController(ITopicsService topicsService) {
        this.topicsService = topicsService;
    }

    // Body: { "name": "L'ablativo assoluto", "imageReference": null }
    @PostMapping("/subjects/{subjectId}/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponseDTO create(@PathVariable long subjectId, @RequestBody @Valid TopicDTO body) {
        return TopicResponseDTO.from(topicsService.saveNewTopic(subjectId, body));
    }

    @GetMapping("/subjects/{subjectId}/topics")
    public List<TopicResponseDTO> findBySubject(@PathVariable long subjectId) {
        return topicsService.findBySubject(subjectId).stream().map(TopicResponseDTO::from).toList();
    }

    @GetMapping("/topics/{topicId}")
    public TopicResponseDTO findById(@PathVariable long topicId) {
        return TopicResponseDTO.from(topicsService.findById(topicId));
    }

    @PutMapping("/topics/{topicId}")
    public TopicResponseDTO update(@PathVariable long topicId, @RequestBody @Valid TopicDTO body) {
        return TopicResponseDTO.from(topicsService.findByIdAndUpdate(topicId, body));
    }

    // Da mostrare prima di eliminare
    @GetMapping("/topics/{topicId}/usages")
    public TopicUsagesDTO usages(@PathVariable long topicId) {
        return topicsService.findUsages(topicId);
    }

    // 409 se l'argomento è usato
    @DeleteMapping("/topics/{topicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long topicId) {
        topicsService.deleteTopic(topicId);
    }
}
