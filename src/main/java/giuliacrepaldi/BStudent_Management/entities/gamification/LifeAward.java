package giuliacrepaldi.BStudent_Management.entities.gamification;

import giuliacrepaldi.BStudent_Management.enums.gamification.LifeAwardReason;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "life_awards", uniqueConstraints = {
        // Idempotenza: lo stesso evento non assegna due vite
        @UniqueConstraint(name = "life_awards_event_id_idx", columnNames = {"event_id"})
}, indexes = {
        @Index(name = "idx_life_awards_student", columnList = "student_id, award_day")
})
// Vita ottenuta con la sfida giornaliera o con il tris della Palestra (v0.10, Blueprint Gamification §7.1, §8.1).
// Il DB impone anche una sola vita della sfida per studente e giorno (indice unico parziale)
public class LifeAward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lifeAwardId;

    @NotNull
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private LifeAwardReason reason;

    // Giorno secondo l'ora italiana
    @NotNull
    @Column(nullable = false)
    private LocalDate awardDay;

    @NotBlank
    @Column(name = "event_id", nullable = false, columnDefinition = "TEXT")
    private String eventId;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime awardedAt;
}
