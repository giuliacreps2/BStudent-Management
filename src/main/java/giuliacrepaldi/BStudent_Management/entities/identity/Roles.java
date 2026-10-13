package giuliacrepaldi.BStudent_Management.entities.identity;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder

@Entity
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long roleId;

    @Column(length = 40, unique = true, nullable = false)
    private String code;
}
