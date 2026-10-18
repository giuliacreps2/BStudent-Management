package giuliacrepaldi.BStudent_Management.entities.progression;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.enums.progression.SourceKind;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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
        name = "skill_credit_entries",
        uniqueConstraints = {
                // Idempotenza: un evento accredita una skill al massimo una volta
                @UniqueConstraint(name = "skill_credit_entries_event_id_skill_id_idx", columnNames = {"event_id", "skill_id"})
        },
        indexes = {
                @Index(name = "idx_skill_credit_entries_student", columnList = "student_id, skill_id"),
                @Index(name = "idx_skill_credit_entries_skill", columnList = "skill_id")
        }
)
public class SkillCreditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creditEntryId;

    @NotBlank
    @Column(name = "event_id", nullable = false, columnDefinition = "TEXT")
    private String eventId;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal delta;

    @NotNull
    @DecimalMin("0.00")
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal previousContribution;

    @NotNull
    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal newContribution;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private SourceKind sourceKind;

    @NotNull
    @Column(nullable = false)
    private Long sourceId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime recordedAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @PrePersist
    @PreUpdate
    private void validateNewContribution() {
        if (previousContribution == null || delta == null || newContribution == null) {
            return;
        }
        BigDecimal expected = previousContribution.add(delta);
        if (newContribution.compareTo(expected) != 0) {
            throw new IllegalStateException("newContribution (%s) must equal previousContribution + delta (%s)"
                    .formatted(newContribution, expected));
        }
    }
}
