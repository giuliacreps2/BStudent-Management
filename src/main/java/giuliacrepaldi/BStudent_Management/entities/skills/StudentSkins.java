package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.entities.identity.Students;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder


@Entity
@Table(
        indexes = {
                @Index(name = "student_id", columnList = "skin_id", unique = true),
                @Index(name = "student_id", columnList = "student_skin_id", unique = true),
                @Index(name = "student_skin_id", columnList = "skin_id", unique = true),
                @Index(name = "idx_student_skins_skin", columnList = "skin_id")
        }
)
public class StudentSkins {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long studentSkinId;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime unlockedAt = OffsetDateTime.now();

    @ManyToOne
    @JoinColumn(name = "studentId", nullable = false)
    private Students student;

    @ManyToOne
    @JoinColumn(name = "skinId", nullable = false)
    private Skins skin;


}
