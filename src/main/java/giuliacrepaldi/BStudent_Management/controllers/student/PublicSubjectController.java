package giuliacrepaldi.BStudent_Management.controllers.student;

import giuliacrepaldi.BStudent_Management.payloads.student.CourseLandingDTO;
import giuliacrepaldi.BStudent_Management.services.student.interfaces.IStudentCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Senza account: SecurityConfig lascia libero /public/**. Solo dati di contenuti pubblicati, nessun dato personale
@RestController
@RequestMapping("/public/subjects")
public class PublicSubjectController {

    private final IStudentCatalogService catalogService;

    public PublicSubjectController(IStudentCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // Landing pubblica della materia: corso di riferimento, badge e prime tappe
    @GetMapping("/{subjectSlug}/landing")
    public CourseLandingDTO landing(@PathVariable String subjectSlug) {
        return catalogService.getSubjectLanding(subjectSlug);
    }
}
