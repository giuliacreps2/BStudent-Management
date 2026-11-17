package giuliacrepaldi.BStudent_Management.payloads.student;

import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;

import java.util.List;

// Pagina "I miei corsi": materie pubblicate, materia attiva e i suoi corsi visibili.
// activeSubject è null se non esiste ancora nessuna materia pubblicata.
// recommendedLevel = ultimo risultato del test di livello nella materia attiva, null se mai svolto;
// hasLevelTest = mostrare l'invito a fare il test
public record CoursesPageDTO(List<SubjectCardDTO> subjects, SubjectCardDTO activeSubject, List<CourseCardDTO> courses,
                             CourseLevel recommendedLevel, boolean hasLevelTest) {
}
