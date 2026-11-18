package giuliacrepaldi.BStudent_Management.entities.attempts;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Lezione letta nel percorso della missione (v0.12): insieme ai checkpoint dice a che punto è lo studente.
// Il Quaderno (NotebookLesson) è un'altra cosa: le lezioni che lo studente possiede
@Entity
@Table(name = "mission_run_lessons", indexes = {
        @Index(name = "idx_mission_run_lessons_lesson", columnList = "lesson_id")
})
public class MissionRunLesson {

    // Chiave composta: (mission_run_id, lesson_id)
    @EmbeddedId
    @Builder.Default
    private MissionRunLessonId id = new MissionRunLessonId();

    @ToString.Exclude
    @MapsId("missionRunId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_run_id")
    private MissionRun missionRun;

    @ToString.Exclude
    @MapsId("lessonId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime readAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MissionRunLessonId implements Serializable {
        private Long missionRunId;
        private Long lessonId;
    }
}
