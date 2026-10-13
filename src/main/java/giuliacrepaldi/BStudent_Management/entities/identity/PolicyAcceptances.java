package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.User;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
public class PolicyAcceptances {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long acceptanceId;

    @Column(length = 80, nullable = false)
    private String documentCode;

    @Column(length = 40, nullable = false)
    private String documentVersion;

    @Column(nullable = false)
    private LocalDateTime acknowledgedAt = LocalDateTime.now();

    @OneToOne
    @JoinColumn
    private User userId;
}
