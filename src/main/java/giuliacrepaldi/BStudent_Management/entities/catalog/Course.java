package giuliacrepaldi.BStudent_Management.entities.catalog;

import giuliacrepaldi.BStudent_Management.enums.catalog.AccessTier;
import giuliacrepaldi.BStudent_Management.enums.catalog.ContentStatus;
import giuliacrepaldi.BStudent_Management.enums.catalog.CourseLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_courses_subject", columnList = "subject_id")
})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    @NotBlank
    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String courseTitle;

    @NotBlank
    @Column(name = "short_description", nullable = false, columnDefinition = "TEXT")
    private String courseShortDescription;

    @NotBlank
    @Column(name = "extended_description", nullable = false, columnDefinition = "TEXT")
    private String courseDescription;

    @Column(name = "cover_reference", columnDefinition = "TEXT")
    private String courseImage;

    // Indirizzo della pagina, unico (v0.9)
    @NotBlank
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String slug;

    // Sottotitolo della pagina del corso
    @Column(columnDefinition = "TEXT")
    private String tagline;

    // Obbligatorio per pubblicare (test di livello, blueprint §8.2); null nei corsi creati prima della v0.9
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT")
    private CourseLevel level;

    // "In evidenza" scelto dall'Admin finché la data non è passata (blueprint §5.8)
    private OffsetDateTime featuredUntil;

    // Tema della mappa (es. roman-city): illustrazioni e posti delle missioni stanno nel frontend
    @Column(columnDefinition = "TEXT")
    private String worldTheme;

    @Column(columnDefinition = "TEXT")
    private String quote;

    @Column(columnDefinition = "TEXT")
    private String quoteAuthor;

    // Bozza di default: visibile agli studenti solo dopo la pubblicazione
    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private ContentStatus status = ContentStatus.draft;

    private OffsetDateTime publishedAt;

    // Valorizzato se e solo se status = withdrawn (chk_courses_withdrawn)
    private OffsetDateTime withdrawnAt;

    // Date editoriali (v0.7), impostate da Hibernate
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    // Default delle missioni che non ne indicano uno proprio (Mission.accessTier null).
    // Premium di default: un corso nuovo resta riservato finché non lo si segna free
    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private AccessTier accessTier = AccessTier.premium;

    // Corso d'ingresso della materia: gratuito dall'inizio alla fine, dà la prima evoluzione a chiunque.
    // Al massimo uno per materia (indice unico parziale nel DB; v0.10, Blueprint Gamification §8-bis)
    @Builder.Default
    @Column(name = "is_entry_course", nullable = false)
    private boolean entryCourse = false;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
}
