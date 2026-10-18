package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "student_life_states")
public class StudentLifeState {

    // La PK coincide con student_id: un solo stato vite per studente
    @Id
    @Column(name = "student_id")
    private Long studentId;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @PositiveOrZero
    @Builder.Default
    @Column(nullable = false)
    private int lives = 5;

    @Positive
    @Builder.Default
    @Column(nullable = false)
    private int baseLives = 5;

    private OffsetDateTime rechargeAt;

    @PositiveOrZero
    @Builder.Default
    @Column(nullable = false)
    private int livesRecoveredInCycle = 0;
}
