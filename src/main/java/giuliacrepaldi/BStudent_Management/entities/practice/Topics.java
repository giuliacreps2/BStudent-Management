package giuliacrepaldi.BStudent_Management.entities.practice;

import giuliacrepaldi.BStudent_Management.entities.catalog.Subjects;
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
                @Index(name = "subject_id", columnList = "name", unique = true)
        }
)
public class Topics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long topicId;

    @Column(name = "name", nullable = false)
    private String topicName;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subjects subject;


    private void validateTopicName(String topicName) {
        if (this.topicName != null && !topicName.trim().isEmpty()) {
            log.info("Topic name is already in use {}", topicName);
        } else {
            log.info("Topic name is empty");
        }
    }
    
}
