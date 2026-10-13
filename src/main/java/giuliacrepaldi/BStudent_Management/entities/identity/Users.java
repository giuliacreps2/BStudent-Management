package giuliacrepaldi.BStudent_Management.entities.identity;

import giuliacrepaldi.BStudent_Management.enums.identity.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

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
    @Column(nullable = false)
    private String passwordHash;
    @Column(length = 100)
    private String firstName;
    @Column(length = 100)
    private String lastName;
    private Date birthDate;
    @Column(length = 40)
    private String phoneNumber;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.valueOf("PENDING_VERIFICATION");

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime deletedAt;
    private LocalDateTime emailVerifiedAt;

}
