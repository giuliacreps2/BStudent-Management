package giuliacrepaldi.BStudent_Management.entities.identity;

import giuliacrepaldi.BStudent_Management.enums.identity.RegistrationRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

// Registrazione in attesa di conferma email (14 anni o più) o del consenso del genitore/tutore.
// users/students nascono solo alla conferma; le richieste scadute o rifiutate vengono cancellate.
@Entity
@Table(name = "registration_requests", indexes = {
        @Index(name = "idx_registration_requests_expiry", columnList = "expires_at")
})
// Unique parziali uq_open_registration_requests_email/_username (status = 'pending'): solo lato DB
@Check(name = "chk_registration_requests_guardian",
        constraints = "requires_guardian_consent = (guardian_email_normalized IS NOT NULL)")
@Check(name = "chk_registration_requests_guardian_email",
        constraints = "guardian_email_normalized IS NULL OR guardian_email_normalized <> email_normalized")
@Check(name = "chk_registration_requests_expiry", constraints = "expires_at > created_at")
public class RegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationRequestId;

    @NotBlank
    @Column(length = 320, nullable = false)
    private String emailNormalized;

    @ToString.Exclude
    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @NotNull
    @Column(nullable = false)
    private LocalDate birthDate;

    @NotBlank
    @Column(length = 80, nullable = false)
    private String username;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    // Calcolato dal server dalla data di nascita, mai accettato dal client
    @Column(nullable = false)
    private boolean requiresGuardianConsent;

    // Solo sotto l'età del consenso digitale
    @Column(length = 320)
    private String guardianEmailNormalized;

    // Versione dell'informativa mostrata nel form di registrazione
    @NotBlank
    @Column(length = 40, nullable = false)
    private String policyVersion;

    // Solo l'hash SHA-256: il token in chiaro esiste solo nell'email
    @ToString.Exclude
    @NotBlank
    @Column(unique = true, nullable = false, columnDefinition = "TEXT")
    private String tokenHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "TEXT")
    private RegistrationRequestStatus status = RegistrationRequestStatus.pending;

    @NotNull
    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Positive
    @Builder.Default
    @Column(nullable = false)
    private short sendCount = 1;

    private OffsetDateTime lastSentAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
