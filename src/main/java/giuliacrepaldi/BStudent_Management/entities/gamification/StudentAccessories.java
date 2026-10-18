package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.entities.identity.Students;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Slf4j

@Entity
@Table(
        indexes = {
                @Index(name = "student_id", columnList = "accessory_id")
        }
)
public class StudentAccessories {

    // Chiave composta: (student_id, accessory_id)
    @EmbeddedId
    private StudentAccessoriesId id;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime redeemedAt = OffsetDateTime.now();

    // Lo studente
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Students student;

    // L'accessorio
    @MapsId("accessoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessory_id", nullable = false)
    private Accessories accessory;


    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StudentAccessoriesId implements Serializable {
        private Long studentId;
        private Long accessoryId;

    }
}
