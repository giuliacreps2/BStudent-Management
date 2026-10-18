package giuliacrepaldi.BStudent_Management.entities.notebook;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
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

@Entity
@Table(name = "notebook_lessons", indexes = {
        @Index(name = "idx_notebook_lessons_lesson", columnList = "lesson_id")
})
public class NotebookLesson {

    // Chiave composta: (student_id, lesson_id)
    @EmbeddedId
    @Builder.Default
    private NotebookLessonId id = new NotebookLessonId();

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ToString.Exclude
    @MapsId("lessonId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime acquiredAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class NotebookLessonId implements Serializable {
        private Long studentId;
        private Long lessonId;
    }
}
