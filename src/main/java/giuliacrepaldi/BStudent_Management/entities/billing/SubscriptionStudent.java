package giuliacrepaldi.BStudent_Management.entities.billing;

import giuliacrepaldi.BStudent_Management.entities.identity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Studente coperto da un abbonamento. Il massimo per piano (max_students) lo verifica il service;
// uno studente è coperto da un solo abbonamento alla volta
@Entity
@Table(name = "subscription_students", indexes = {
        @Index(name = "idx_subscription_students_subscription", columnList = "subscription_id")
})
// Unique parziale uq_open_subscription_students_student (removed_at IS NULL): solo lato DB
@Check(name = "chk_subscription_students_removed", constraints = "removed_at IS NULL OR removed_at >= added_at")
public class SubscriptionStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriptionStudentId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime addedAt = OffsetDateTime.now();

    // Valorizzato quando lo studente esce dal piano o l'abbonamento termina
    private OffsetDateTime removedAt;
}
