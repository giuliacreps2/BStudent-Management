package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long studentId;

    @Column(length = 80, unique = true, nullable = false)
    private String username;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private Schools school;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private Users user;


}
