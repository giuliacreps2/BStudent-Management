package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.entities.skills.Skill;
import giuliacrepaldi.BStudent_Management.enums.catalog.SubjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectId;

    @NotBlank
    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String subjectName;

    // Indirizzo della pagina, unico (v0.9)
    @NotBlank
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String imageReference;

    // Bozza = nascosta agli studenti; le nuove materie nascono in bozza
    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private SubjectStatus status = SubjectStatus.draft;

    // Prima pubblicazione: resta anche se la materia torna in bozza
    private OffsetDateTime publishedAt;

    // Guida fissa che presenta le missioni nel briefing (blueprint §5.9)
    @Column(columnDefinition = "TEXT")
    private String guideName;

    @Column(columnDefinition = "TEXT")
    private String guideImageReference;

    // Fallback dei pesi skill degli esercizi di missione (100%); null = pesi sempre espliciti
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_skill_id")
    private Skill defaultSkill;
}
