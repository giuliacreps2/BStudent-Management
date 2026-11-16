package giuliacrepaldi.BStudent_Management.entities.practice;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subject;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "topics", uniqueConstraints = {
        @UniqueConstraint(name = "topics_subject_id_name_idx", columnNames = {"subject_id", "name"})
})
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long topicId;

    @NotBlank
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String topicName;

    // Illustrazione facoltativa nella Palestra; senza, si usa quella della skill (v0.9)
    @Column(columnDefinition = "TEXT")
    private String imageReference;

    // Classe di riferimento: anno di scuola superiore (1-5) da cui di solito si studia l'argomento.
    // null = per tutti. Non blocca, dà solo un avviso (v0.10, Blueprint Gamification §3-bis)
    @Min(1)
    @Max(5)
    private Short referenceYear;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
