package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString

@Entity
@Table(indexes = {
        @Index(name = "idx_students_school", columnList = "school_id")
})
public class Students {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long studentId;

    @Column(length = 80, unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDate createdAt = LocalDate.now();

    private LocalDate lastModified;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private Schools schoolId;

    @ManyToOne
    @JoinColumn(unique = true, nullable = false)
    private Users userId;


}
