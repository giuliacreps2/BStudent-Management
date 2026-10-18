package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "character_selections", indexes = {
        @Index(name = "idx_character_selections_evolution", columnList = "student_skin_id, evolution_id")
})
public class CharacterSelection {

    @Id
    @Column(name = "student_id")
    private Long studentId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_skin_id", nullable = false)
    private StudentSkin studentSkin;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evolution_id", nullable = false)
    private SkinEvolution evolution;

    // Aggiornato ad ogni cambio di selezione
    @UpdateTimestamp
    @Column(name = "selected_at", nullable = false)
    private OffsetDateTime selectedAt;
}
