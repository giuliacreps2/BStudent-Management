package giuliacrepaldi.BStudent_Management.entities.catalog;


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
public class Subjects {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long subjectId;

    @Column(name = "name", nullable = false)
    private String subjectName;

    private void validateSubjectName() {
        if (this.subjectName != null && !this.subjectName.trim().isEmpty()) {
            log.info("Subject name is valid and contains{}", subjectName.trim());
        } else {
            log.info("Subject name is null or empty");
        }
    }
}
