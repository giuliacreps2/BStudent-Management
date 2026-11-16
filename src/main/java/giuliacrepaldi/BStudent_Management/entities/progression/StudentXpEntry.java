package giuliacrepaldi.BStudent_Management.entities.progression;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "student_xp_entries", uniqueConstraints = {
        // Una missione dà XP una volta sola: ripetizioni e doppi invii non riaccreditano
        @UniqueConstraint(name = "student_xp_entries_student_id_mission_id_idx", columnNames = {"student_id", "mission_id"})
}, indexes = {
        @Index(name = "idx_student_xp_entries_mission", columnList = "mission_id")
})
// XP dello studente (v0.10, Blueprint Gamification §1): una riga per missione superata la prima volta.
// Gli XP si fissano in quel momento; il totale è la somma del registro. Le FK sono verificate dal DB
public class StudentXpEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long xpEntryId;

    @NotNull
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @NotNull
    @Column(name = "mission_id", nullable = false)
    private Long missionId;

    @NotNull
    @Column(name = "mission_attempt_id", nullable = false)
    private Long missionAttemptId;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal xp;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime awardedAt;
}
