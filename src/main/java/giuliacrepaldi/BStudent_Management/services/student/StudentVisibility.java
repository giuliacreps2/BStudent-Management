package giuliacrepaldi.BStudent_Management.services.student;

import giuliacrepaldi.BStudent_Management.entities.catalog.Course;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.catalog.MissionStep;
import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;

import java.util.List;

// L'unica regola di visibilità per lo studente (package-info, TODO comuni): un contenuto si vede se è
// pubblicato e lo sono anche tutti i suoi contenitori, materia compresa. Nessun service la riscrive.
// Il lucchetto premium è un'altra cosa: il contenuto si vede, ma si apre solo con l'accesso (§5.6)
public final class StudentVisibility {

    private StudentVisibility() {
    }

    public static boolean isVisible(Subject subject) {
        return subject.getStatus() == SubjectStatus.published;
    }

    public static boolean isVisible(Course course) {
        return course.getStatus() == ContentStatus.published && isVisible(course.getSubject());
    }

    public static boolean isVisible(Mission mission) {
        return mission.getStatus() == ContentStatus.published && isVisible(mission.getCourse());
    }

    // Lezione pubblicata; esercizio pubblicato nella missione e non ritirato nel banco
    // (stessa logica di MissionsService.visibleSteps)
    public static boolean isVisible(MissionStep step) {
        if (step.isExercise()) {
            return step.getMissionExercise().getStatus() == ContentStatus.published
                    && step.getMissionExercise().getQuestionBankEntry().getStatus() != ContentStatus.withdrawn;
        }
        return step.getLesson().getStatus() == ContentStatus.published;
    }

    public static List<MissionStep> visibleSteps(List<MissionStep> steps) {
        return steps.stream().filter(StudentVisibility::isVisible).toList();
    }

    // Voce della Palestra: pubblicata, esercizio del banco non ritirato, materia dell'argomento pubblicata
    public static boolean isVisible(PracticeExercise exercise) {
        return exercise.getStatus() == ContentStatus.published
                && exercise.getQuestionBankEntry().getStatus() != ContentStatus.withdrawn
                && isVisible(exercise.getTopic().getSubject());
    }

    public static boolean isVisible(Resource resource) {
        return resource.getStatus() == ContentStatus.published && isVisible(resource.getSubject());
    }
}
