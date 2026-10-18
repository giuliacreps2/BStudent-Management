package giuliacrepaldi.BStudent_Management.entities.practice;

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

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
