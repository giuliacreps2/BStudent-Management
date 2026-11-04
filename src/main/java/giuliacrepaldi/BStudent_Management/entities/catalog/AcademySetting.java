package giuliacrepaldi.BStudent_Management.entities.catalog;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "academy_settings")
// Cambiare questi valori non riscrive lo storico: exercise_assessments.passing_threshold
// e mission_skill_contributions.factor_snapshot conservano il valore usato
public class AcademySetting {

    // Riga singola di configurazione: il DB impone settings_id = 1 (nessuna IDENTITY)
    @Id
    @Column(name = "settings_id")
    private Short academySettingsId;

    // Soglia di superamento sul risultato ponderato (blueprint §9.3, §10.1)
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @DecimalMax("100.00")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal passingThreshold;

    // Fattore dei punti skill delle Missioni, 0.1000 = 10% (blueprint §10.3)
    @NotNull
    @DecimalMin("0")
    @DecimalMax("1")
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal missionSkillFactor;

    // Default applicati alla creazione di un esercizio di missione
    @Positive
    @Column(nullable = false)
    private long defaultAllowedDurationMs;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal defaultMaxPoints;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
