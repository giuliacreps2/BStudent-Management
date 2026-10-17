package giuliacrepaldi.BStudent_Management.entities.exercises;

import giuliacrepaldi.BStudent_Management.entities.catalog.Lessons;
import giuliacrepaldi.BStudent_Management.entities.catalog.Missions;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@Slf4j

@Entity
@Table(
        indexes = {
                @Index(name = "mission_id", columnList = "question_bank_entry_id", unique = true)
        }
)
public class MissionExercises {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long missionExerciseId;

    @ManyToOne
    @JoinColumn(name = "mission_id", nullable = false)
    private Missions mission;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false, unique = true)
    private Lessons lesson;

    @ManyToOne
    @JoinColumn(name = "question_bank_entry_id", nullable = false)
    private QuestionBankEntries questionBankEntry;
}
