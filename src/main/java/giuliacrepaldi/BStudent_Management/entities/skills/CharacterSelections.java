package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.entities.identity.Students;
import jakarta.persistence.*;
import lombok.*;

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
                @Index(name = "idx_character_selections_evolution", columnList = "student_skin_id"),
                @Index(name = "idx_character_selections_evolution", columnList = "evolution_id")
        }
)
public class CharacterSelections {

    @Id
    @Column(name = "student_id")
    private Long studentId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "student_id", nullable = false)
    private Students student;

    @ManyToOne
    @JoinColumn(name = "student_skin_id", nullable = false)
    private StudentSkins studentSkin;

    @ManyToOne
    @JoinColumn(name = "evolution_id", nullable = false)
    private SkinEvolutions evolution;

    @Builder.Default
    @Column(name = "selected_at", nullable = false)
    private OffsetDateTime selectedAt = OffsetDateTime.now();
}
