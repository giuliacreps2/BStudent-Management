package giuliacrepaldi.BStudent_Management.entities.notebook;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lesson;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "lesson_notes", indexes = {
        @Index(name = "idx_lesson_notes_lesson", columnList = "lesson_id")
})
// FK composta (student_id, lesson_id) -> notebook_lessons verificata dal DB:
// si puo' annotare solo una lezione presente nel Taccuino
public class LessonNote {

    // Chiave composta: (student_id, lesson_id)
    @EmbeddedId
    @Builder.Default
    private LessonNoteId id = new LessonNoteId();

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

    @NotBlank
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String noteText;

    // Contatore di revisione gestito dal service: non e' @Version perche' Hibernate
    // inizializzerebbe a 0, mentre il DB richiede version >= 1
    @Positive
    @Builder.Default
    @Column(nullable = false)
    private long version = 1L;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class LessonNoteId implements Serializable {
        private Long studentId;
        private Long lessonId;
    }
}
