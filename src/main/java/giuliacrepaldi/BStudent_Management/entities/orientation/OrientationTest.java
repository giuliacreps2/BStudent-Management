package giuliacrepaldi.BStudent_Management.entities.orientation;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import giuliacrepaldi.BStudent_Management.enums.orientation.OrientationTestKind;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "orientation_tests", uniqueConstraints = {
        // Un test per materia e per tipo (v0.9)
        @UniqueConstraint(name = "uq_orientation_tests_subject_kind", columnNames = {"subject_id", "kind"})
})
public class OrientationTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orientationTestId;

    // Fino a due test per materia: orientamento della Palestra e livello del corso
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // Fissato alla creazione: le opzioni hanno regole diverse secondo il tipo
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    private OrientationTestKind kind;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
}
