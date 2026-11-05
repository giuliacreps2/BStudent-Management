package giuliacrepaldi.BStudent_Management.controllers.catalog;

import giuliacrepaldi.BStudent_Management.payloads.catalog.CourseDTO;
import giuliacrepaldi.BStudent_Management.payloads.catalog.CourseResponseDTO;
import giuliacrepaldi.BStudent_Management.services.catalog.interfaces.ICoursesService;
import jakarta.validation.Valid;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Solo ADMIN: SecurityConfig protegge /admin/academy/**
@RestController
@RequestMapping("/admin/academy")
public class CourseController {

    private final ICoursesService coursesService;

    public CourseController(ICoursesService coursesService) {
        this.coursesService = coursesService;
    }

    //CORSI DELLA MATERIA: la materia è nel path, il corso nasce draft
    @PostMapping("/subjects/{subjectId}/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponseDTO create(@PathVariable long subjectId, @RequestBody @Valid CourseDTO body) {
        return CourseResponseDTO.from(coursesService.saveNewCourse(subjectId, body));
    }

    @GetMapping("/subjects/{subjectId}/courses")
    public PagedModel<CourseResponseDTO> findBySubject(@PathVariable long subjectId,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(defaultValue = "courseTitle") String sortBy) {
        return new PagedModel<>(coursesService.findBySubject(subjectId, page, size, sortBy)
                .map(CourseResponseDTO::from));
    }

    //CORSI: tutti gli stati, è la vista dell'Admin
    @GetMapping("/courses")
    public PagedModel<CourseResponseDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(defaultValue = "courseTitle") String sortBy) {
        return new PagedModel<>(coursesService.findAll(page, size, sortBy).map(CourseResponseDTO::from));
    }

    @GetMapping("/courses/{courseId}")
    public CourseResponseDTO findById(@PathVariable long courseId) {
        return CourseResponseDTO.from(coursesService.findById(courseId));
    }

    @PutMapping("/courses/{courseId}")
    public CourseResponseDTO update(@PathVariable long courseId, @RequestBody @Valid CourseDTO body) {
        return CourseResponseDTO.from(coursesService.findByIdAndUpdate(courseId, body));
    }

    //STATO: cambi di stato senza body; 409 se la transizione non è ammessa
    @PatchMapping("/courses/{courseId}/publish")
    public CourseResponseDTO publish(@PathVariable long courseId) {
        return CourseResponseDTO.from(coursesService.publishCourse(courseId));
    }

    @PatchMapping("/courses/{courseId}/withdraw")
    public CourseResponseDTO withdraw(@PathVariable long courseId) {
        return CourseResponseDTO.from(coursesService.withdrawCourse(courseId));
    }

    // Solo bozze mai pubblicate e senza missioni, altrimenti 409
    @DeleteMapping("/courses/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long courseId) {
        coursesService.deleteCourse(courseId);
    }
}
