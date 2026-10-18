package giuliacrepaldi.BStudent_Management.entities.orientation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "orientation_questions", uniqueConstraints = {
        @UniqueConstraint(name = "orientation_questions_orientation_test_id_sort_order_idx",
                columnNames = {"orientation_test_id", "sort_order"})
})
public class OrientationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orientationQuestionId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orientation_test_id", nullable = false)
    private OrientationTest orientationTest;

    @NotBlank
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int questionSortOrder;
}
