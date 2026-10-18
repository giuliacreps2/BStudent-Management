package giuliacrepaldi.BStudent_Management.entities.practice;

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
@Table(name = "orientation_options", uniqueConstraints = {
        @UniqueConstraint(name = "orientation_options_orientation_question_id_sort_order_idx",
                columnNames = {"orientation_question_id", "sort_order"})
})
public class OrientationOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orientationOptionId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orientation_question_id", nullable = false)
    private OrientationQuestion orientationQuestion;

    @NotBlank
    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int optionSortOrder;
}
