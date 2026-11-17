package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;

// Materia vista dallo studente (blueprint §5.8). locked = pubblicata ma senza corsi pubblicati:
// la tagline diventa "Presto disponibile"
public record SubjectCardDTO(String slug, String name, String tagline, String imageReference, boolean locked) {

    public static SubjectCardDTO from(Subject subject, boolean hasCourses) {
        return new SubjectCardDTO(subject.getSlug(), subject.getSubjectName(),
                hasCourses ? subject.getTagline() : "Presto disponibile", subject.getImageReference(), !hasCourses);
    }
}
