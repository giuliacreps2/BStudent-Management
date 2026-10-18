package giuliacrepaldi.BStudent_Management.entities.practice;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "orientation_recommendations", indexes = {
        @Index(name = "idx_orientation_recs_exercise", columnList = "practice_exercise_id")
})
public class OrientationRecommendation {

    // Chiave composta: (orientation_option_id, practice_exercise_id)
    @EmbeddedId
    @Builder.Default
    private OrientationRecommendationId id = new OrientationRecommendationId();

    @ToString.Exclude
    @MapsId("orientationOptionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orientation_option_id")
    private OrientationOption orientationOption;

    @ToString.Exclude
    @MapsId("practiceExerciseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_exercise_id")
    private PracticeExercise practiceExercise;

    @Positive
    @Column(name = "sort_order", nullable = false)
    private int recommendationSortOrder;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class OrientationRecommendationId implements Serializable {
        private Long orientationOptionId;
        private Long practiceExerciseId;
    }
}
