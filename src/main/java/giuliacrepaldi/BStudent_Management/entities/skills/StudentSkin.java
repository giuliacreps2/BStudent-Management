package giuliacrepaldi.BStudent_Management.entities.skills;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(
        name = "student_skins",
        uniqueConstraints = {
                @UniqueConstraint(name = "student_skins_student_id_skin_id_idx", columnNames = {"student_id", "skin_id"}),
                // Target delle FK composte di character_selections e student_skin_evolutions
                @UniqueConstraint(name = "student_skins_student_id_student_skin_id_idx", columnNames = {"student_id", "student_skin_id"}),
                @UniqueConstraint(name = "student_skins_student_skin_id_skin_id_idx", columnNames = {"student_skin_id", "skin_id"})
        },
        indexes = {
                @Index(name = "idx_student_skins_skin", columnList = "skin_id")
        }
)
public class StudentSkin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentSkinId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime unlockedAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_id", nullable = false)
    private Skin skin;
}
