package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
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
@Table(name = "hint_uses", indexes = {
        @Index(name = "idx_hint_uses_attempt", columnList = "mission_attempt_id, student_id"),
        @Index(name = "idx_hint_uses_accessory", columnList = "student_id, accessory_id")
})
// FK composte verificate dal DB:
// (mission_attempt_id, student_id) -> mission_attempts, (student_id, accessory_id) -> student_accessories
public class HintUse {

    // Chiave composta: (student_id, mission_attempt_id, hint_id)
    @EmbeddedId
    @Builder.Default
    private HintUseId id = new HintUseId();

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ToString.Exclude
    @MapsId("missionAttemptId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_attempt_id")
    private MissionAttempt missionAttempt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessory_id", nullable = false)
    private Accessory accessory;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime openedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class HintUseId implements Serializable {
        private Long studentId;
        private Long missionAttemptId;

        // Identificativo dell'aiuto: nessuna tabella hints nello schema attuale
        private Long hintId;
    }
}
