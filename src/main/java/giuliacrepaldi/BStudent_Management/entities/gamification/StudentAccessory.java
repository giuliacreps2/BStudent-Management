package giuliacrepaldi.BStudent_Management.entities.gamification;

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
@Table(name = "student_accessories", indexes = {
        // student_id e' gia' coperto dalla PK composta
        @Index(name = "idx_student_accessories_accessory", columnList = "accessory_id")
})
public class StudentAccessory {

    // Chiave composta: (student_id, accessory_id)
    @EmbeddedId
    @Builder.Default
    private StudentAccessoryId id = new StudentAccessoryId();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime redeemedAt;

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ToString.Exclude
    @MapsId("accessoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessory_id", nullable = false)
    private Accessory accessory;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StudentAccessoryId implements Serializable {
        private Long studentId;
        private Long accessoryId;
    }
}
