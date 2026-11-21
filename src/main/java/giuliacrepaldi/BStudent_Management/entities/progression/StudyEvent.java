package giuliacrepaldi.BStudent_Management.entities.progression;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.catalog.LessonPage;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.practice.PracticeExercise;
import giuliacrepaldi.BStudent_Management.entities.resources.Resource;
import giuliacrepaldi.BStudent_Management.enums.progression.StudyEventKind;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
// Registro di studio (v0.9, blueprint §10.5): si aggiunge e non si modifica mai.
// Un evento ha un solo oggetto secondo il tipo (vincoli del DB): lezione (con la pagina facoltativa), Risorsa
// o voce della Palestra consigliata. active_ms c'è per le letture, mai per i consigli aperti
@Table(name = "study_events")
public class StudyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyEventId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, updatable = false)
    private Student student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private StudyEventKind kind;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", updatable = false)
    private Lesson lesson;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_page_id", updatable = false)
    private LessonPage lessonPage;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", updatable = false)
    private Resource resource;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_exercise_id", updatable = false)
    private PracticeExercise practiceExercise;

    @NotNull
    @Column(nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Positive
    @Column(updatable = false)
    private Long activeMs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
