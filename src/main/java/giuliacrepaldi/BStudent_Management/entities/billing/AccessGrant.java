package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import giuliacrepaldi.BStudent_Management.entities.identity.User;
import giuliacrepaldi.BStudent_Management.enums.billing.AccessGrantSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Accesso premium senza pagamento: utenti beta, promozioni, staff.
// Non è un contratto: alla scadenza non parte nessun addebito
@Entity
@Table(name = "access_grants", indexes = {
        @Index(name = "idx_access_grants_student", columnList = "student_id")
})
@Check(name = "chk_access_grants_period", constraints = "ends_at IS NULL OR ends_at > starts_at")
public class AccessGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accessGrantId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private AccessGrantSource source;

    @NotNull
    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime startsAt = OffsetDateTime.now();

    // Null = senza scadenza (es. staff)
    private OffsetDateTime endsAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by_user_id")
    private User grantedBy;

    private OffsetDateTime revokedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
