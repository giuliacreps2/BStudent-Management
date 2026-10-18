package giuliacrepaldi.BStudent_Management.entities.orientation;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "orientation_tests")
public class OrientationTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orientationTestId;

    // Un solo test di orientamento per materia
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false, unique = true)
    private Subject subject;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;
}
