package giuliacrepaldi.BStudent_Management.controllers.identity.catalog;

import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonPageDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonPageResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.LessonResponseDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.ReorderDTO;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ILessonsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**
@RestController
@RequestMapping("/admin/academy")
public class LessonController {

    private final ILessonsService lessonsService;

    public LessonController(ILessonsService lessonsService) {
        this.lessonsService = lessonsService;
    }

    //LEZIONI DELLA MISSIONE: la missione è nel path, la lezione nasce draft in coda all'ordine
    @PostMapping("/missions/{missionId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponseDTO create(@PathVariable long missionId, @RequestBody @Valid LessonDTO body) {
        return LessonResponseDTO.from(lessonsService.saveNewLesson(missionId, body));
    }

    // In ordine, tutti gli stati: è la vista dell'Admin
    @GetMapping("/missions/{missionId}/lessons")
    public List<LessonResponseDTO> findByMission(@PathVariable long missionId) {
        return lessonsService.findByMission(missionId).stream().map(LessonResponseDTO::from).toList();
    }

    // Body: { "ids": [...] } con tutti e soli gli ID delle lezioni della missione, nel nuovo ordine
    @PutMapping("/missions/{missionId}/lessons/order")
    public List<LessonResponseDTO> reorder(@PathVariable long missionId, @RequestBody @Valid ReorderDTO body) {
        return lessonsService.reorderLessons(missionId, body.ids()).stream().map(LessonResponseDTO::from).toList();
    }

    //LEZIONI
    @GetMapping("/lessons/{lessonId}")
    public LessonResponseDTO findById(@PathVariable long lessonId) {
        return LessonResponseDTO.from(lessonsService.findById(lessonId));
    }

    @PutMapping("/lessons/{lessonId}")
    public LessonResponseDTO update(@PathVariable long lessonId, @RequestBody @Valid LessonDTO body) {
        return LessonResponseDTO.from(lessonsService.findByIdAndUpdate(lessonId, body));
    }

    //STATO: 409 se la transizione non è ammessa, la lezione non ha pagine o non ha l'esercizio collegato
    @PatchMapping("/lessons/{lessonId}/publish")
    public LessonResponseDTO publish(@PathVariable long lessonId) {
        return LessonResponseDTO.from(lessonsService.publishLesson(lessonId));
    }

    @PatchMapping("/lessons/{lessonId}/withdraw")
    public LessonResponseDTO withdraw(@PathVariable long lessonId) {
        return LessonResponseDTO.from(lessonsService.withdrawLesson(lessonId));
    }

    // Solo bozze mai pubblicate e senza esercizio collegato; le pagine vengono eliminate con la lezione
    @DeleteMapping("/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long lessonId) {
        lessonsService.deleteLesson(lessonId);
    }

    //PAGINE DELLA LEZIONE: un solo contenuto per pagina (testo, immagine o video)
    @PostMapping("/lessons/{lessonId}/pages")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonPageResponseDTO createPage(@PathVariable long lessonId, @RequestBody @Valid LessonPageDTO body) {
        return LessonPageResponseDTO.from(lessonsService.saveNewLessonPage(lessonId, body));
    }

    @GetMapping("/lessons/{lessonId}/pages")
    public List<LessonPageResponseDTO> findPages(@PathVariable long lessonId) {
        return lessonsService.findPagesByLesson(lessonId).stream().map(LessonPageResponseDTO::from).toList();
    }

    @PutMapping("/lessons/{lessonId}/pages/order")
    public List<LessonPageResponseDTO> reorderPages(@PathVariable long lessonId, @RequestBody @Valid ReorderDTO body) {
        return lessonsService.reorderLessonPages(lessonId, body.ids()).stream().map(LessonPageResponseDTO::from).toList();
    }

    @PutMapping("/pages/{lessonPageId}")
    public LessonPageResponseDTO updatePage(@PathVariable long lessonPageId, @RequestBody @Valid LessonPageDTO body) {
        return LessonPageResponseDTO.from(lessonsService.findPageByIdAndUpdate(lessonPageId, body));
    }

    @DeleteMapping("/pages/{lessonPageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePage(@PathVariable long lessonPageId) {
        lessonsService.deleteLessonPage(lessonPageId);
    }
}
