package giuliacrepaldi.BStudent_Management.entities.identity;

import giuliacrepaldi.BStudent_Management.enums.identity.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
public class Users {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;
    @Column(unique = true, nullable = false)
    private String emailNormalized;

    @ToString.Exclude
    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 100)
    private String firstName;
    @Column(length = 100)
    private String lastName;
    private LocalDate birthDate;
    @Column(length = 40)
    private String phoneNumber;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.pending_verification;

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
    @Builder.Default
    @Column
    private OffsetDateTime updatedAt = OffsetDateTime.now();
    private OffsetDateTime deletedAt;
    private OffsetDateTime emailVerifiedAt;

    @ManyToMany
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))

    @ToString.Exclude
    @Builder.Default
    private Set<Roles> roles = new HashSet<>();

}
