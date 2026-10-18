package giuliacrepaldi.BStudent_Management.entities.progression;

import giuliacrepaldi.BStudent_Management.entities.attempts.MissionAttempt;
import giuliacrepaldi.BStudent_Management.entities.catalog.Mission;
import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(
        name = "mission_records",
        uniqueConstraints = {
                // Un tentativo alimenta un solo record
                @UniqueConstraint(name = "uq_mission_records_attempt", columnNames = "mission_attempt_id")
        },
        indexes = {
                @Index(name = "idx_mission_records_mission", columnList = "mission_id")
        }
)
public class MissionRecord {

    // Chiave composta: (student_id, mission_id), il miglior risultato per missione
    @EmbeddedId
    @Builder.Default
    private MissionRecordId id = new MissionRecordId();

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ToString.Exclude
    @MapsId("missionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private Mission mission;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_attempt_id", nullable = false)
    private MissionAttempt missionAttempt;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MissionRecordId implements Serializable {
        private Long studentId;
        private Long missionId;
    }
}
