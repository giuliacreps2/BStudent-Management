package giuliacrepaldi.BStudent_Management.entities.skills;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "student_evolution_stages")
// Stadio raggiunto dallo studente (v0.10, Blueprint Gamification §4.5, §4.6): è dello studente, non del
// personaggio, e non si cancella mai. L'immagine si prende da skin_evolutions del personaggio scelto
public class StudentEvolutionStage {

    // Chiave composta: (student_id, stage_number). Una seconda assegnazione dello stesso stadio non è possibile
    @EmbeddedId
    @Builder.Default
    private StudentEvolutionStageId id = new StudentEvolutionStageId();

    @Column(nullable = false, updatable = false)
    private OffsetDateTime reachedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StudentEvolutionStageId implements Serializable {
        @Column(name = "student_id")
        private Long studentId;
        @Column(name = "stage_number")
        private Integer stageNumber;
    }
}
