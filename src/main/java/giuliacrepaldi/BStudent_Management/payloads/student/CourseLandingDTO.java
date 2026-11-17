package giuliacrepaldi.BStudent_Management.payloads.student;

import java.util.List;

// Landing pubblica della materia, senza account (blueprint §5.8): corso di riferimento, badge calcolati
// (missioni ed esercizi) e le sue prime tappe, al massimo 5. referenceCourse è null se la materia non ha
// ancora corsi pubblicati. Testi, FAQ e personaggi restano statici nel frontend
public record CourseLandingDTO(SubjectCardDTO subject, CourseCardDTO referenceCourse, int missionsCount,
                               long exercisesCount, List<LandingStepDTO> steps) {
}
