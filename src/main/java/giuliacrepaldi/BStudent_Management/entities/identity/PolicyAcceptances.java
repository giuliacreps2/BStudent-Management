package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

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

    @Builder.Default
    @Column(nullable = false)
    private OffsetDateTime acknowledgedAt = OffsetDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}
