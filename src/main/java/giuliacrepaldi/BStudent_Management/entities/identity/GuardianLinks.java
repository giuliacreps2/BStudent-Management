package giuliacrepaldi.BStudent_Management.entities.identity;

import giuliacrepaldi.BStudent_Management.enums.identity.Relationship;
import giuliacrepaldi.BStudent_Management.enums.identity.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(indexes = {
        @Index(name = "idx_guardian_links_student", columnList = "student_id")
})
public class GuardianLinks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long guardianUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Relationship relationship;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "student_Id", nullable = false)
    private Students studentId;

    @ManyToOne
    @JoinColumn(unique = true, nullable = false)
    private Users userId;


}
