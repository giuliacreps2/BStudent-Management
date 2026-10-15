package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.entities.identity.Students;
import jakarta.persistence.*;
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
@Table(
        indexes = {
                @Index(name = "event_id", columnList = "skill_id", unique = true)
        }
)
public class SkillCreditEntries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creditEntryId;

    @Column(name = "event_id", nullable = false, columnDefinition = "TEXT")
    private String eventId;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal delta;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal previousContribution;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal newContribution;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime recordedAt = OffsetDateTime.now();

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Students student;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skills skill;

    //TODO DA RENDERE
    // "source_kind" text [not null, check: `source_kind IN ('practice','mission')`]
    //  "source_id" bigint [not null]

    private void validateDelta() {
        if (delta == null) {
            delta = BigDecimal.ZERO;
        }
        if (delta.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Delta cannot be negative");
        }
    }

    private void validatePreviousContribution() {
        if (previousContribution.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Previous contribution cannot be negative");
        }
    }

    //todo migliorare la verbosità dell'operazione
    private void validateNewContribution() {
        if (!(this.newContribution == (previousContribution.add(delta)))) {
            throw new IllegalStateException("newContribution is null");
        }
    }


}
