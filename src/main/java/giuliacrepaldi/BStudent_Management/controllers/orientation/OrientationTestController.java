package giuliacrepaldi.BStudent_Management.controllers.orientation;

import giuliacrepaldi.BStudent_Management.payloads.catalog.ReorderDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationOptionDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationQuestionDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationTestDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationTestDetailDTO;
import giuliacrepaldi.BStudent_Management.payloads.orientation.OrientationTestResponseDTO;
import giuliacrepaldi.BStudent_Management.services.orientation.interfaces.IOrientationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**.
// Le modifiche restituiscono il test intero aggiornato, con l'elenco dei problemi (vuoto = pronto)
@RestController
@RequestMapping("/admin/academy")
public class OrientationTestController {

    private final IOrientationService orientationService;

    public OrientationTestController(IOrientationService orientationService) {
        this.orientationService = orientationService;
    }

    //Test

    // Body: { "kind": "course_level", "title": "Da che livello parto?" }
    @PostMapping("/subjects/{subjectId}/orientation-tests")
    @ResponseStatus(HttpStatus.CREATED)
    public OrientationTestDetailDTO create(@PathVariable long subjectId, @RequestBody @Valid OrientationTestDTO body) {
        return orientationService.getDetail(orientationService.saveNewTest(subjectId, body).getOrientationTestId());
    }

    @GetMapping("/subjects/{subjectId}/orientation-tests")
    public List<OrientationTestResponseDTO> findBySubject(@PathVariable long subjectId) {
        return orientationService.findBySubject(subjectId).stream().map(OrientationTestResponseDTO::from).toList();
    }

    @GetMapping("/orientation-tests/{testId}")
    public OrientationTestDetailDTO findById(@PathVariable long testId) {
        return orientationService.getDetail(testId);
    }

    @PutMapping("/orientation-tests/{testId}")
    public OrientationTestDetailDTO update(@PathVariable long testId, @RequestBody @Valid OrientationTestDTO body) {
        return orientationService.updateTest(testId, body);
    }

    // 409 se qualche studente ha già svolto il test
    @DeleteMapping("/orientation-tests/{testId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long testId) {
        orientationService.deleteTest(testId);
    }

    //Domande

    @PostMapping("/orientation-tests/{testId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public OrientationTestDetailDTO addQuestion(@PathVariable long testId, @RequestBody @Valid OrientationQuestionDTO body) {
        return orientationService.addQuestion(testId, body);
    }

    // Body: { "ids": [questionId, ...] } con tutti e soli gli ID delle domande del test
    @PutMapping("/orientation-tests/{testId}/questions/order")
    public OrientationTestDetailDTO reorderQuestions(@PathVariable long testId, @RequestBody @Valid ReorderDTO body) {
        return orientationService.reorderQuestions(testId, body.ids());
    }

    @PutMapping("/orientation-questions/{questionId}")
    public OrientationTestDetailDTO updateQuestion(@PathVariable long questionId,
                                                   @RequestBody @Valid OrientationQuestionDTO body) {
        return orientationService.updateQuestion(questionId, body);
    }

    @DeleteMapping("/orientation-questions/{questionId}")
    public OrientationTestDetailDTO deleteQuestion(@PathVariable long questionId) {
        return orientationService.deleteQuestion(questionId);
    }

    //Opzioni

    // Body test di livello: { "text": "...", "level": "beginner" }
    // Body test di orientamento: { "text": "...", "recommendations": [ { "practiceExerciseId": 4, "priority": 1 } ] }
    @PostMapping("/orientation-questions/{questionId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public OrientationTestDetailDTO addOption(@PathVariable long questionId, @RequestBody @Valid OrientationOptionDTO body) {
        return orientationService.addOption(questionId, body);
    }

    @PutMapping("/orientation-questions/{questionId}/options/order")
    public OrientationTestDetailDTO reorderOptions(@PathVariable long questionId, @RequestBody @Valid ReorderDTO body) {
        return orientationService.reorderOptions(questionId, body.ids());
    }

    @PutMapping("/orientation-options/{optionId}")
    public OrientationTestDetailDTO updateOption(@PathVariable long optionId, @RequestBody @Valid OrientationOptionDTO body) {
        return orientationService.updateOption(optionId, body);
    }

    // 409 se qualche studente ha già scelto questa risposta
    @DeleteMapping("/orientation-options/{optionId}")
    public OrientationTestDetailDTO deleteOption(@PathVariable long optionId) {
        return orientationService.deleteOption(optionId);
    }
}
