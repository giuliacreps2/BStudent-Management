package giuliacrepaldi.BStudent_Management.entities.notebook;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
@Table(name = "blog_post_favorites")
public class BlogPostFavorite {

    // Chiave composta: (student_id, blog_post_id)
    @EmbeddedId
    @Builder.Default
    private BlogPostFavoriteId id = new BlogPostFavoriteId();

    @ToString.Exclude
    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime addedAt;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class BlogPostFavoriteId implements Serializable {
        private Long studentId;

        // Riferimento a un articolo esterno: nessuna tabella blog_posts nello schema
        private Long blogPostId;
    }
}
